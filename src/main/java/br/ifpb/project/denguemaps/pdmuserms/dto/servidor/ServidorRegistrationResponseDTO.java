package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;

import java.util.UUID;

public record ServidorRegistrationResponseDTO(
        // Dados do Keycloak
        String username,
        String nome,
        String cpf,
        String rg

) {
}
