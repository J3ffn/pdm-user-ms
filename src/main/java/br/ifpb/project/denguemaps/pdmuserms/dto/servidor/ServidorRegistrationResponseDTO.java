package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;

import java.util.UUID;

public record ServidorRegistrationResponseDTO(
        // Dados do Keycloak
        UUID fkkeycloakId,
        String username,
        String email,
        String firstName,
        String lastName,

        // Dados de Negócio (PostgreSQL)
        String nome,
        String cpf,
        String rg,

        // Dados das Foreign Keys
        Secretaria secretaria,
        Municipio municipio
) {
}
