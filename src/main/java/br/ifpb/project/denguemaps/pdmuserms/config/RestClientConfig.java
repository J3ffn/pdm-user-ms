package br.ifpb.project.denguemaps.pdmuserms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // ✅ CORREÇÃO: Usa o padrão builder() seguido de build()
        return RestClient.builder().build();
    }

    // Se você estivesse configurando o URL base (Opcional, mas boa prática):
    /*
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Bean
    public RestClient keycloakRestClient() {
        return RestClient.builder()
                .baseUrl(keycloakUrl)
                .build();
    }
    */
}