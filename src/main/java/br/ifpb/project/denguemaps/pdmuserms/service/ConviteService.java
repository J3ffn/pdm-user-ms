package br.ifpb.project.denguemaps.pdmuserms.service;


import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.RegistrarServidorRequest;
import br.ifpb.project.denguemaps.pdmuserms.entity.Convite;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.entity.Servidor;
import br.ifpb.project.denguemaps.pdmuserms.dto.convite.CreateConviteRequest;
import br.ifpb.project.denguemaps.pdmuserms.repository.ConviteRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.SecretariaRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.ServidorRepository;
import br.ifpb.project.denguemaps.pdmuserms.security.exception.ExternalAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final SecretariaRepository secretariaRepository;
    private final MunicipioRepository municipioRepository;
    private final ServidorRepository servidorRepository;

    private final RestClient restClient;
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.userName}")
    private String adminUsername;
    @Value("${keycloak.password}")
    private String adminPassword;

    private record AdminTokenResponse(String access_token, int expires_in) {}
    @Transactional
    public Convite criarConvite(CreateConviteRequest dto, Servidor criador) {
        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new IllegalArgumentException("Secretaria não encontrada"));
        Municipio municipio = municipioRepository.findById(dto.municipioId())
                .orElseThrow(() -> new IllegalArgumentException("Município não encontrado"));

        Convite convite = new Convite();

        convite.setToken(UUID.randomUUID().toString());
        convite.setSecretaria(secretaria);
        convite.setMunicipio(municipio);
        convite.setServidorCriador(criador);
        convite.setPerfilDestino(dto.perfilDestino() != null ? dto.perfilDestino() : "AGENTE_SANITARIO");
        convite.setExpiresAt(OffsetDateTime.now().plusDays(2));
        convite.setCreatedAt(OffsetDateTime.now());

        return conviteRepository.save(convite);
    }

    @Transactional
    public Servidor registrarServidor(String token, RegistrarServidorRequest req) {
        Convite convite = conviteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Convite inválido"));

        if (convite.getUsedAt() != null) {
            throw new IllegalStateException("Convite já utilizado");
        }
        if (OffsetDateTime.now().isAfter(convite.getExpiresAt())) {
            throw new IllegalStateException("Convite expirado");
        }

        Servidor servidor = returnEntity(convite, req);
        servidor = saveEntity(servidor);

        servidor.setRefKeycloakId(saveKeycloak(req, servidor)); // salvando no keycloak, recuperando como FK
        servidor.setStatusCreated(Boolean.TRUE);
        servidor = saveEntity(servidor);

        convite.setUsedAt(OffsetDateTime.now());
        conviteRepository.save(convite);

        return servidor;
    }


    private Servidor returnEntity(Convite convite, RegistrarServidorRequest registrarServidorRequest){
        Servidor servidor = new Servidor();
        servidor.setStatusCreated(Boolean.FALSE);
        servidor.setSecretaria(convite.getSecretaria());
        servidor.setNome(registrarServidorRequest.nome());
        servidor.setRg(registrarServidorRequest.rg());
        servidor.setCpf(registrarServidorRequest.cpf());
        servidor.setMunicipio(convite.getMunicipio());
        return servidor;
    }

    private Servidor saveEntity(Servidor servidor){
        return servidorRepository.save(servidor);
    }

    private UUID saveKeycloak(RegistrarServidorRequest registrarServidorRequest, Servidor servidor){
        Object keycloakUserRepresentation = buildKeycloakUser(registrarServidorRequest);
        String adminApiUrl = String.format("%s/admin/realms/%s/users", keycloakUrl, realm);
        String tokenAdmin = getAdminAccessToken();
        try {
            var response = restClient.post()
                    .uri(adminApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(keycloakUserRepresentation)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode() == HttpStatus.CREATED) {

                String locationHeader = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                String keycloakUserId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                UUID idKeyCloak = UUID.fromString(keycloakUserId);
                log.info("Usuário criado no Keycloak com ID: {}", keycloakUserId);
                return idKeyCloak;

            } else {
                // Cenário de erro de status não-exceção, mas com response válido
                throw new ExternalAuthException("Falha desconhecida ao registrar usuário no Keycloak. Status: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            // Esta exceção é lançada pelo RestClient/Spring quando há status 4xx ou 5xx
            log.error("Falha ao registrar usuário: HTTP {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalAuthException("Falha ao registrar usuário no Keycloak. " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Erro inesperado durante o registro.", e);
            throw new RuntimeException("Erro interno durante o registro do servidor.", e);
        }
    }

    private Object buildKeycloakUser(RegistrarServidorRequest dto) {
        return Map.of(
                "username", dto.nome(),
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

    private String getAdminAccessToken() {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, realm);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", adminUsername);
        formData.add("password", adminPassword);

        try {
            AdminTokenResponse response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData) // O RestClient tentará serializar este Map
                    .retrieve()
                    .body(AdminTokenResponse.class);

            // Verificação de null (embora 'body' de RestClient normalmente lance exceção em 4xx/5xx)
            if (response == null || response.access_token() == null) {
                throw new ExternalAuthException("Resposta do Keycloak inválida ou vazia.");
            }

            return response.access_token();

        } catch (HttpClientErrorException e) {
            // Captura erros 4xx (Unauthorized, Forbidden, etc.)
            log.error("Falha ao obter token de admin do Keycloak. HTTP Status: {}", e.getStatusCode(), e);
            throw new ExternalAuthException("Falha ao obter token de admin: Credenciais inválidas.");
        } catch (Exception e) {
            // Captura outros erros (Conexão, I/O)
            log.error("Erro inesperado ao conectar ao Keycloak.", e);
            throw new RuntimeException("Erro interno ao obter token de admin.", e);
        }
    }
}