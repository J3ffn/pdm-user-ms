package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;

import java.util.UUID;

public record RegistrarServidorRequest(
        String nome,
        String firstName,
        String lastName,
        String cpf,
        String rg,
        String email,
        String password,
        UUID refKeycloakId
) {}