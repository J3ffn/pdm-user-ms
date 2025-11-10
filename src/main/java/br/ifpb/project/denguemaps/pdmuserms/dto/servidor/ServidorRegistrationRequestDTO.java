package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ServidorRegistrationRequestDTO(@NotBlank String username,
                                             @Email String email,
                                             @NotBlank String password,
                                             @NotBlank String firstName,
                                             @NotBlank String lastName,

                                             // ⭐️ CAMPOS ADICIONADOS PARA SINCRONIZAÇÃO COM POSTGRES
                                             @NotBlank String nome, // Nome completo do servidor
                                             @NotBlank @Size(min = 11, max = 11) String cpf,
                                             @NotBlank @Size(min = 9, max = 9) String rg,

                                             // UUIDs dos IDs externos (podem ser nulos)
                                             UUID fkSecretariaId,
                                             UUID fkMunicipioId) {
}
