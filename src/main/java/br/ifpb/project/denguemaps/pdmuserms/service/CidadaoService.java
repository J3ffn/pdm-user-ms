package br.ifpb.project.denguemaps.pdmuserms.service;


import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoAtualizarDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoCriarDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmuserms.entity.Endereco;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Report;
import br.ifpb.project.denguemaps.pdmuserms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.EnderecoRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmuserms.security.exception.ExternalAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CidadaoService {
    private final CidadaoRepository cidadaoRepository;
    private final EnderecoRepository enderecoRepository;
    private final MunicipioRepository municipioRepository;
    private final ReportService reportService;

    // Dados Keycloak:
    private record AdminTokenResponse(String access_token, int expires_in) {}
    private record RoleRepresentation(String id, String name) {}
    private record KeycloakUserRepresentation(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled,
            // Inclua outros campos que você precisa, como realmRoles, clientRoles, etc.
            Long createdTimestamp
    ) {}
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


    public CidadaoResponseDTO registrarCidadao(CidadaoCriarDTO cidadaoCriarDTO){
        Municipio municipio = municipioRepository
                .findById(cidadaoCriarDTO.getFkMunicipioID())
                .orElseThrow(() -> new IllegalArgumentException("Municipio não encontrado"));
        Endereco enderecoExistente = buscarEnderecoExistenteRetornar(cidadaoCriarDTO, municipio.getId());
        if(enderecoExistente != null){
            // Endereco ja cadastrado, cidadao mora na mesma residencia de outro cidadao cadastrado no sistema.
            // Setando endereco novamente, para salvar o ID de endereco:
            // Evitando assim, gerar um novo endereco com os mesmos dados repetidos.
            cidadaoCriarDTO.setEndereco(enderecoExistente);
        }else {
            // Nao encontrado, novo endereco, set de referencia para o municipio.
            // Setagem de municipio deve ser feita aqui, uma vez que se ja existe o endereco:
            // logo ja possui municipio(referindo-se a condicao a cima).
            cidadaoCriarDTO.getEndereco().setMunicipio(municipio);
        }
        // Adicionar logica de login keycloak, tentar cadastrar cidadao no sistema:
        Cidadao cidadao = formatarCidadaoRetornar(cidadaoCriarDTO, saveKeycloak(cidadaoCriarDTO));
        CidadaoResponseDTO cidadaoResponseDTO = retornarCidadaoResponseDTO(salvarCidadaoRetornar(cidadao));
        cidadaoResponseDTO.setEmail(cidadaoCriarDTO.getEmail());
        cidadaoResponseDTO.setPrimeiroNome(cidadaoCriarDTO.getPrimeiroNome());
        cidadaoResponseDTO.setUltimoNome(cidadaoCriarDTO.getUltimoNome());
        cidadaoResponseDTO.setFkMunicipioID(municipio.getId());
        return cidadaoResponseDTO;
    }
    
    public CidadaoResponseDTO atualizarCidadao(CidadaoAtualizarDTO cidadaoAtualizarDTO, UUID idCidadao, String token){
        // Verificando existencia do cidadao:
        Cidadao cidadao = cidadaoRepository.findById(idCidadao)
                .orElseThrow(() -> new IllegalArgumentException("Cidadao não encontrado"));
        // Verificando existencia do novo municipio selecionado pelo cidadao:
        Municipio municipio = municipioRepository
                .findById(cidadaoAtualizarDTO.getFkMunicipioID())
                .orElseThrow(() -> new IllegalArgumentException("Municipio não encontrado"));
        // Criando massa para buscar endereco ja existente, obs: cidadao se mudando para um endereco ja cadastrado.
        CidadaoCriarDTO enderecoDados = new CidadaoCriarDTO();
        // Inserindo dados de endereco para pesquisar:
        enderecoDados.setEndereco(cidadaoAtualizarDTO.getEndereco());
        // Buscando efetivamente:
        Endereco enderecoExistente = buscarEnderecoExistenteRetornar(enderecoDados, municipio.getId());
        
        if(enderecoExistente != null){
            // Endereco ja cadastrado, cidadao mora na mesma residencia de outro cidadao cadastrado no sistema.
            // Setando endereco novamente, para salvar o ID de endereco:
            // Evitando assim, gerar um novo endereco com os mesmos dados repetidos.
            cidadaoAtualizarDTO.setEndereco(enderecoExistente);
        }else {
            // Nao encontrado, novo endereco, set de referencia para o municipio.
            // Setagem de municipio deve ser feita aqui, uma vez que se ja existe o endereco:
            // logo ja possui municipio(referindo-se a condicao a cima).
            cidadaoAtualizarDTO.getEndereco().setMunicipio(municipio);
        }
        // Setando dados no cidadao:
        formatarCidadaoAtualizar(cidadaoAtualizarDTO, cidadao);
        // Salvando alteracoes de cidadao no Keycloak:
        updateKeycloak(cidadaoAtualizarDTO, cidadao.getRefKeycloakId(), token);
        // Persistindo dados de cidadao e retorando reponse:
        CidadaoResponseDTO cidadaoResponseDTO = retornarCidadaoResponseDTO(salvarCidadaoRetornar(cidadao));
        cidadaoResponseDTO.setPrimeiroNome(cidadaoAtualizarDTO.getPrimeiroNome());
        cidadaoResponseDTO.setUltimoNome(cidadaoAtualizarDTO.getUltimoNome());
        cidadaoResponseDTO.setEmail(cidadaoAtualizarDTO.getEmail());
        cidadaoResponseDTO.setFkMunicipioID(cidadaoAtualizarDTO.getFkMunicipioID());
        return cidadaoResponseDTO;
    }

    public void deletarCidadao(UUID idCidadao, String token){
        deletarCidadaoEspecifico(idCidadao, token);
    }

    public CidadaoResponseDTO buscarCidadaoEspecificoId(UUID uuid, String token){
        Cidadao cidadao = cidadaoRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cidadao não encontrado"));
        KeycloakUserRepresentation keycloakUserRepresentation = getKeycloakUser(cidadao.getRefKeycloakId(), token);
        CidadaoResponseDTO cidadaoResponseDTO = retornarCidadaoResponseDTO(cidadao);
        formatarCidadaoKeycloak(cidadaoResponseDTO, keycloakUserRepresentation);
        cidadaoResponseDTO.setFkMunicipioID(cidadao.getEndereco().getMunicipio().getId());
        return cidadaoResponseDTO;
    }

    public List<CidadaoResponseDTO> buscarTodoCidadao(String token){

        // 1. Buscar todos os Cidadãos do Banco de Dados (Postgres)
        List<Cidadao> listaCidadao = cidadaoRepository.findAll();

        // 2. Buscar TODOS os Usuários do Keycloak em uma ÚNICA Requisição
        List<KeycloakUserRepresentation> keycloakUsers = getAllKeycloakUsers(token);

        // 3. Mapear e cruzar os dados
        return mapearCidadaoComKeycloak(listaCidadao, keycloakUsers);
    }



    private Endereco buscarEnderecoExistenteRetornar(CidadaoCriarDTO cidadaoCriarDTO, UUID fkMunicipioId){
        // Extraindo dados do endereco, para buscar a existencia de um ja cadastrado.
        // Se identificado algum igual ao que foi repassado na criacao do usuario, o mesmo e um membro familiar que reside no mesmo endereco.
        Endereco enderecoBuscar = cidadaoCriarDTO.getEndereco();
        String logradouro = enderecoBuscar.getLogradouro();
        String cep = enderecoBuscar.getCep();
        String numero = enderecoBuscar.getNumero();
        return enderecoRepository.findAllByMunicipioIdAndLogradouroAndNumeroAndCep(
                fkMunicipioId, logradouro, numero, cep
        );
    }

    private Cidadao salvarCidadaoRetornar(Cidadao cidadao){
        return cidadaoRepository.save(cidadao);
    }
    private void deletarCidadaoEspecifico(UUID uuid, String token){
        Cidadao cidadao = cidadaoRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cidadao não encontrado"));
        // Buscando todos os report do cidadao deletado:
        reportService.deletarTodoReportCidadao(uuid);
        // Deletando usuario na tabela:
        cidadaoRepository.deleteById(uuid);
        // Deletando usuario no keycloak:
        deleteKeycloakUser(cidadao.getRefKeycloakId(), token);
    }
    private Cidadao formatarCidadaoRetornar(CidadaoCriarDTO cidadaoCriacao, UUID fkKeycloakId){
        Cidadao cidadao = new Cidadao();
        cidadao.setCpf(cidadaoCriacao.getCpf());
        cidadao.setEndereco(cidadaoCriacao.getEndereco());
        cidadao.setNome(cidadaoCriacao.getNome());
        cidadao.setRefKeycloakId(fkKeycloakId);
        return cidadao;
    }
    
    private void formatarCidadaoAtualizar(CidadaoAtualizarDTO cidadaoAtualizarDTO, Cidadao cidadaoAtual){
        cidadaoAtual.setCpf(cidadaoAtualizarDTO.getCpf());
        cidadaoAtual.setEndereco(cidadaoAtualizarDTO.getEndereco());
        cidadaoAtual.setNome(cidadaoAtualizarDTO.getNome());
    }

    private CidadaoResponseDTO retornarCidadaoResponseDTO(Cidadao cidadao){
        CidadaoResponseDTO cidadaoResponseDTO = new CidadaoResponseDTO();
        cidadaoResponseDTO.setId(cidadao.getId());
        cidadaoResponseDTO.setNome(cidadao.getNome());
        cidadaoResponseDTO.setCpf(cidadao.getCpf());
        return cidadaoResponseDTO;
    }

    private void formatarCidadaoKeycloak(
            CidadaoResponseDTO cidadaoResponseDTO, KeycloakUserRepresentation keycloakUserRepresentation){
        cidadaoResponseDTO.setPrimeiroNome(keycloakUserRepresentation.firstName);
        cidadaoResponseDTO.setUltimoNome(keycloakUserRepresentation.lastName);
        cidadaoResponseDTO.setEmail(keycloakUserRepresentation.email);
    }
    private List<CidadaoResponseDTO> mapearCidadaoComKeycloak(
            List<Cidadao> listaCidadao,
            List<KeycloakUserRepresentation> keycloakUsers) {

        // 1. Criar um Mapa para busca O(1) por CPF (Username)
        Map<String, KeycloakUserRepresentation> keycloakUserMap = keycloakUsers.stream()
                // Assumimos que o campo 'username' do Keycloak é o CPF
                .collect(Collectors.toMap(KeycloakUserRepresentation::username, Function.identity()));

        // 2. Mapear a lista de Cidadãos
        return listaCidadao.stream()
                .map(cidadao -> {
                    CidadaoResponseDTO dto = retornarCidadaoResponseDTO(cidadao);

                    // Tenta encontrar o usuário Keycloak pelo CPF (Username)
                    KeycloakUserRepresentation keycloakUser = keycloakUserMap.get(cidadao.getCpf());

                    if (keycloakUser != null) {
                        formatarCidadaoKeycloak(dto, keycloakUser);
                    } else {
                        // Tratar caso onde o Keycloak não tem o usuário (opcional)
                        log.warn("Usuário Keycloak não encontrado para o CPF: {}", cidadao.getCpf());
                    }

                    // Mapear o ID do Município (como você fez no método individual)
                    if (cidadao.getEndereco() != null && cidadao.getEndereco().getMunicipio() != null) {
                        dto.setFkMunicipioID(cidadao.getEndereco().getMunicipio().getId());
                    } else {
                        // Gerenciar Endereço/Município LAZY ou nulo
                        // Você deve ter cuidado com FetchType.LAZY aqui
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // metodos keycloak:
    private UUID saveKeycloak(CidadaoCriarDTO cidadaoCriarDTO){
        Object keycloakUserRepresentation = buildKeycloakUser(cidadaoCriarDTO);
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
                String roleCidadao = "cidadao";
                assignRealmRole(keycloakUserId, tokenAdmin, roleCidadao);
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
    
    private void updateKeycloak(CidadaoAtualizarDTO cidadaoAtualizarDTO, UUID fkKeycloakId, String token){
        String updateUrl = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, fkKeycloakId.toString());

        // Prepara o corpo da requisição APENAS com os campos do Keycloak
        Map<String, Object> keycloakUpdateRepresentation = Map.of(
                "firstName", cidadaoAtualizarDTO.getPrimeiroNome(),
                "lastName", cidadaoAtualizarDTO.getUltimoNome(),
                "email", cidadaoAtualizarDTO.getEmail()
                // Outros campos como username e enabled não são alterados
        );

        try {
            restClient.put()
                    .uri(updateUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(keycloakUpdateRepresentation)
                    .retrieve()
                    .toBodilessEntity(); // Retorna 204 No Content em caso de sucesso (ou 200)

        } catch (HttpClientErrorException e) {
            log.error("Falha ao atualizar usuário no Keycloak: HTTP {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalAuthException("Falha ao atualizar Keycloak: " + e.getResponseBodyAsString());
        }
    }

    public void deleteKeycloakUser(UUID keycloakUserId, String adminAccessToken) {

        // 1. Constrói a URL do endpoint de usuário
        String userDeleteUrl = String.format("%s/admin/realms/%s/users/%s",
                keycloakUrl, realm, keycloakUserId.toString());

        try {
            log.info("Tentando deletar usuário Keycloak com ID: {}", keycloakUserId);

            // 2. Executa a requisição DELETE
            restClient.delete()
                    .uri(userDeleteUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                    .retrieve()
                    .toBodilessEntity(); // A exclusão bem-sucedida retorna um 204 No Content

            log.info("Usuário Keycloak ID {} deletado com sucesso.", keycloakUserId);

        } catch (HttpStatusCodeException e) {
            // Captura erros 4xx/5xx (ex: 404 Not Found se o usuário não existir)
            log.error("Falha ao deletar usuário Keycloak {}. HTTP Status: {} - Body: {}",
                    keycloakUserId, e.getStatusCode(), e.getResponseBodyAsString());

            // Você pode querer relançar uma exceção específica de negócio aqui
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ExternalAuthException("Usuário Keycloak não encontrado para o ID: " + keycloakUserId);
            }
            throw new ExternalAuthException("Falha ao deletar usuário no Keycloak. " + e.getResponseBodyAsString());

        } catch (Exception e) {
            // Erros de rede ou outros erros inesperados
            log.error("Erro inesperado durante a exclusão do usuário Keycloak.", e);
            throw new RuntimeException("Erro interno ao se comunicar com o Keycloak.", e);
        }
    }

    public KeycloakUserRepresentation getKeycloakUser(UUID keycloakUserId, String adminAccessToken) {

        // 1. Constrói a URL do endpoint de usuário
        String userGetUrl = String.format("%s/admin/realms/%s/users/%s",
                keycloakUrl, realm, keycloakUserId.toString());

        try {
            log.info("Tentando consultar usuário Keycloak com ID: {}", keycloakUserId);

            // 2. Executa a requisição GET
            KeycloakUserRepresentation userRepresentation = restClient.get()
                    .uri(userGetUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                    .retrieve()
                    // 3. Mapeia o corpo da resposta para a classe de representação
                    .body(KeycloakUserRepresentation.class);

            if (userRepresentation != null) {
                log.info("Usuário Keycloak ID {} consultado com sucesso. Username: {}", keycloakUserId, userRepresentation.username());
            }

            return userRepresentation;

        } catch (HttpStatusCodeException e) {
            // Captura erros 4xx/5xx (ex: 404 Not Found se o usuário não existir)
            log.error("Falha ao consultar usuário Keycloak {}. HTTP Status: {} - Body: {}",
                    keycloakUserId, e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Retorna null ou lança uma exceção de "Não Encontrado"
                return null;
            }
            throw new ExternalAuthException("Falha ao consultar usuário no Keycloak. " + e.getResponseBodyAsString());

        } catch (Exception e) {
            // Erros de rede ou outros erros inesperados
            log.error("Erro inesperado durante a consulta do usuário Keycloak.", e);
            throw new RuntimeException("Erro interno ao se comunicar com o Keycloak.", e);
        }
    }


    private List<KeycloakUserRepresentation> getAllKeycloakUsers(String adminAccessToken) {

        // A API do Keycloak tem paginação. 100 é um bom valor de limite (limit)
        final int PAGE_SIZE = 100;
        int offset = 0;
        List<KeycloakUserRepresentation> allUsers = new ArrayList<>();
        boolean morePages = true;

        String usersListUrl = String.format("%s/admin/realms/%s/users?max=%d&first=",
                keycloakUrl, realm, PAGE_SIZE);

        try {
            while (morePages) {
                String paginatedUrl = usersListUrl + offset;

                // O uso de ParameterizedTypeReference é crucial para deserializar List<T>
                List<KeycloakUserRepresentation> page = restClient.get()
                        .uri(paginatedUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<KeycloakUserRepresentation>>() {});

                if (page == null || page.isEmpty()) {
                    morePages = false;
                } else {
                    allUsers.addAll(page);
                    offset += PAGE_SIZE;

                    // Critério de parada: se a página retornar menos que o tamanho máximo, é a última.
                    if (page.size() < PAGE_SIZE) {
                        morePages = false;
                    }
                }
            }
            log.info("Total de {} usuários do Keycloak recuperados.", allUsers.size());
            return allUsers;

        } catch (HttpStatusCodeException e) {
            log.error("Falha ao buscar todos os usuários do Keycloak. HTTP Status: {} - Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalAuthException("Erro ao buscar usuários do Keycloak para mapeamento.");
        }
    }

    private Object buildKeycloakUser(CidadaoCriarDTO dto) {
        return Map.of(
                "username", dto.getCpf(),
                "email", dto.getEmail(),
                "firstName", dto.getPrimeiroNome(),
                "lastName", dto.getUltimoNome(),
                "enabled", true,
                "credentials", List.of(
                        Map.of(
                                "type", "password",
                                "value", dto.getSenha(),
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
            CidadaoService.AdminTokenResponse response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData) // O RestClient tentará serializar este Map
                    .retrieve()
                    .body(CidadaoService.AdminTokenResponse.class);

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

    private void assignRealmRole(String keycloakUserId, String tokenAdmin, String roleName) {
        // 1. URL para buscar a lista de roles (usaremos para encontrar o ID da role 'roleName')
        String rolesUrl = String.format("%s/admin/realms/%s/roles/%s", keycloakUrl, realm, roleName);

        // 1.1. Buscar a representação da Role (para obter o ID)
        CidadaoService.RoleRepresentation roleRepresentation;
        try {
            roleRepresentation = restClient.get()
                    .uri(rolesUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                    .retrieve()
                    .body(CidadaoService.RoleRepresentation.class);

            if (roleRepresentation == null || roleRepresentation.id() == null) {
                throw new ExternalAuthException("A Role de Reino '" + roleName + "' foi encontrada, mas o ID estava ausente.");
            }

            log.info("Role de Reino '{}' encontrada com ID: {}", roleName, roleRepresentation.id());

        } catch (HttpStatusCodeException e) {
            log.error("Falha ao buscar a Role de Reino '{}': HTTP {} - Body: {}", roleName, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalAuthException("Falha ao buscar a Role de Reino '" + roleName + "' no Keycloak.");
        }

        // 2. URL para atribuir a role ao usuário
        String userRolesUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakUrl, realm, keycloakUserId);

        // 2.1. Construir o payload de atribuição. O Keycloak espera uma lista da representação da Role.
        List<Map<String, Object>> rolesPayload = List.of(
                Map.of(
                        "id", roleRepresentation.id(),
                        "name", roleRepresentation.name()
                        // Outros campos como 'containerId' não são estritamente necessários para a atribuição
                )
        );

        // 2.2. POST para atribuir a role
        try {
            restClient.post()
                    .uri(userRolesUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(rolesPayload)
                    .retrieve()
                    .toBodilessEntity(); // Resposta esperada é 204 No Content/200 OK

            log.info("Role de Reino '{}' atribuída com sucesso ao usuário {}", roleName, keycloakUserId);

        } catch (HttpStatusCodeException e) {
            log.error("Falha ao atribuir a Role '{}' ao usuário {}: HTTP {} - Body: {}",
                    roleName, keycloakUserId, e.getStatusCode(), e.getResponseBodyAsString());
            // Dependendo da sua regra de negócio, você pode lançar uma exceção
            throw new ExternalAuthException("Falha ao atribuir a Role de Reino ao usuário.");
        }
    }
}