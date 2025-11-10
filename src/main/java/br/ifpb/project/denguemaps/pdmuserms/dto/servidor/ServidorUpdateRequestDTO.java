package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ServidorUpdateRequestDTO(
        // ✅ NOVO CAMPO CHAVE: Usado para buscar no PostgreSQL
        @NotBlank @Size(min = 11, max = 11) String cpf,
        // Campos para Keycloak
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email, // O email é usado para localizar o usuário no Keycloak

        // Campos para PostgreSQL
        @NotBlank String nome,
        @NotBlank @Size(min = 9, max = 9) String rg,

        // FKs (Podem ser nulos, se for opcional)
        UUID fkSecretariaId,
        UUID fkMunicipioId
) { }
