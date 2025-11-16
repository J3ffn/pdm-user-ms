package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorRegistrationRequestDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorUpdateRequestDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorRegistrationResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorUpdateResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.entity.Servidor;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.SecretariaRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.ServidorRepository;
import br.ifpb.project.denguemaps.pdmuserms.security.exception.ExternalAuthException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Usaremos Lombok para injeção de ServidorRepository
public class ServidorService {

    private static final Logger log = LoggerFactory.getLogger(ServidorService.class); // Ajuste do nome da classe
    private final ServidorRepository servidorRepository;
    private final SecretariaRepository secretariaRepository;
    private final MunicipioRepository municipioRepository;
    private final RestClient restClient;
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.username}")
    private String adminUsername;
    @Value("${keycloak.password}")
    private String adminPassword;


    // MÉTODO DE PERSISTÊNCIA SÍNCRONO
    public Servidor saveServidor(Servidor servidor) {
        // ✅ Chamada SÍNCRONA do JPA
        return servidorRepository.save(servidor);
    }

    public ServidorUpdateResponseDTO updateServidor(ServidorUpdateRequestDTO updateRequestDTO, String adminToken) {
        Servidor servidor = servidorRepository.findByCpf(updateRequestDTO.cpf())
                .orElseThrow(() -> new RuntimeException("Servidor não encontrado no banco de dados com o CPF fornecido."));

        UUID keycloakId = servidor.getRefKeycloakId();
        if (keycloakId == null) {
            throw new ExternalAuthException("O Servidor encontrado não possui FK Keycloak ID. O registro está incompleto.");
        }

        servidor = returnUpdate(updateRequestDTO, servidor);
        Servidor updatedEntity = servidorRepository.save(servidor);

        updateKeycloakUser(keycloakId, updateRequestDTO, adminToken);
        return new ServidorUpdateResponseDTO(
                updateRequestDTO.firstName(),
                updateRequestDTO.lastName(),
                updateRequestDTO.email(),
                updatedEntity.getNome(),
                updatedEntity.getRg(),
                updatedEntity.getSecretaria(),
                updatedEntity.getMunicipio()
        );
    }

    public Servidor getFindByKeycloakID(String sub){
        return servidorRepository.findByRefKeycloakId(UUID.fromString(sub))
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }

    /**
     * Busca um Servidor por CPF no banco de dados.
     * @param cpf O CPF do servidor a ser buscado.
     * @return DTO com os dados do Servidor.
     */
    public ServidorRegistrationResponseDTO getServidorByCpf(String cpf, String adminToken) {

        // 1. BUSCA A ENTIDADE NO POSTGRES PELO CPF
        Servidor servidor = servidorRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Servidor não encontrado com o CPF: " + cpf));

        UUID keycloakId = servidor.getRefKeycloakId();

        if (keycloakId == null) {
            throw new ExternalAuthException("O Servidor encontrado não possui FK Keycloak ID. Não é possível buscar dados do Keycloak.");
        }

        // 2. BUSCA OS DETALHES NO KEYCLOAK
        Map<String, Object> keycloakDetails = fetchKeycloakUserDetails(keycloakId, adminToken);

        // 3. Mapeia a Entidade e os Detalhes do Keycloak para o DTO de Resposta
        return new ServidorRegistrationResponseDTO(
                keycloakId,
                (String) keycloakDetails.get("username"),
                (String) keycloakDetails.get("email"),
                (String) keycloakDetails.get("firstName"),
                (String) keycloakDetails.get("lastName"),
                servidor.getNome(),
                servidor.getCpf(),
                servidor.getRg(),
                servidor.getSecretaria(),
                servidor.getMunicipio()
        );
    }


    // ✅ NOVO MÉTODO AUXILIAR PARA BUSCAR DETALHES DO KEYCLOAK
    private Map<String, Object> fetchKeycloakUserDetails(UUID keycloakId, String adminToken) {
        String fetchUrl = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, keycloakId.toString());

        try {
            // Realiza a chamada REST ao Keycloak
            return restClient.get()
                    .uri(fetchUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalAuthException("Usuário Keycloak não encontrado com ID: " + keycloakId);
        } catch (Exception e) {
            log.error("Erro ao buscar detalhes do Keycloak para ID {}.", keycloakId, e);
            throw new ExternalAuthException("Erro ao buscar detalhes do Keycloak: " + e.getMessage());
        }
    }

    private void updateKeycloakUser(UUID keycloakId, ServidorUpdateRequestDTO dto, String adminToken) {

        String updateUrl = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, keycloakId.toString());

        // Prepara o corpo da requisição APENAS com os campos do Keycloak
        Map<String, Object> keycloakUpdateRepresentation = Map.of(
                "firstName", dto.cpf(),
                "lastName", dto.lastName(),
                "email", dto.email()
                // Outros campos como username e enabled não são alterados
        );

        try {
            restClient.put()
                    .uri(updateUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(keycloakUpdateRepresentation)
                    .retrieve()
                    .toBodilessEntity(); // Retorna 204 No Content em caso de sucesso (ou 200)

        } catch (HttpClientErrorException e) {
            log.error("Falha ao atualizar usuário no Keycloak: HTTP {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalAuthException("Falha ao atualizar Keycloak: " + e.getResponseBodyAsString());
        }
    }

    // Métodos auxiliares (Mantidos sem Mono)

    private UUID findKeycloakIdByEmail(String email, String adminToken) {
        String searchUrl = String.format("%s/admin/realms/%s/users?email=%s", keycloakUrl, realm, email);

        // Keycloak retorna uma lista de UserRepresentation (Map<String, Object>)
        try {
            List<Map<String, Object>> users = restClient.get()
                    .uri(searchUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}); // Retorna uma lista

            if (users == null || users.isEmpty()) {
                throw new ExternalAuthException("Usuário não encontrado no Keycloak pelo email.");
            }

            // Pega o primeiro usuário e extrai o 'id'
            String idString = (String) users.get(0).get("id");
            return UUID.fromString(idString);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalAuthException("Usuário não encontrado no Keycloak.");
        } catch (Exception e) {
            throw new ExternalAuthException("Erro ao buscar ID do Keycloak: " + e.getMessage());
        }
    }
    private Object buildKeycloakUser(ServidorRegistrationRequestDTO dto) {
        // ... (lógica inalterada)
        return Map.of(
                "username", dto.username(),
                "email", dto.email(),
                "firstName", dto.firstName(),
                "lastName", dto.lastName(),
                "enabled", true,
                "credentials", List.of(
                        Map.of(
                                "type", "password",
                                "value", dto.password(),
                                "temporary", false
                        )
                )
        );
    }

    private Servidor returnEntity(ServidorRegistrationRequestDTO servidorRegistrationRequestDTO){
        Secretaria secretaria = secretariaRepository.findById(servidorRegistrationRequestDTO.fkSecretariaId()).get();
        Municipio municipio = municipioRepository.findById(servidorRegistrationRequestDTO.fkMunicipioId()).get();

        Servidor servidor = new Servidor();
        servidor.setStatusCreated(Boolean.FALSE);
        servidor.setSecretaria(secretaria);
        servidor.setMunicipio(municipio);
        servidor.setCpf(servidorRegistrationRequestDTO.cpf());
        servidor.setNome(servidorRegistrationRequestDTO.nome());
        servidor.setRg(servidorRegistrationRequestDTO.rg());
        return servidor;
    }

    private Servidor returnUpdate(ServidorUpdateRequestDTO servidorUpdateRequestDTO, Servidor servidor){
        Secretaria secretaria = secretariaRepository.findById(servidorUpdateRequestDTO.fkSecretariaId()).get();
        Municipio municipio = municipioRepository.findById(servidorUpdateRequestDTO.fkMunicipioId()).get();
        Servidor updateEntity = new Servidor();
        updateEntity.setId(servidor.getId());
        updateEntity.setSecretaria(secretaria);
        updateEntity.setMunicipio(municipio);
        updateEntity.setNome(servidorUpdateRequestDTO.nome());
        updateEntity.setRg(servidorUpdateRequestDTO.rg());
        updateEntity.setStatusCreated(Boolean.TRUE);
        updateEntity.setCpf(servidor.getCpf());
        updateEntity.setRefKeycloakId(servidor.getRefKeycloakId());
        return updateEntity;
    }
}
