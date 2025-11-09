package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;

import java.util.UUID;

public record RegistrarServidorRequest(
        String nome,
        String cpf,
        String rg,
        UUID refKeycloakId
) {}