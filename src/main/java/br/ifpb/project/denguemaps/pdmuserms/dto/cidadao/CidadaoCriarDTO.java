package br.ifpb.project.denguemaps.pdmuserms.dto.cidadao;

import br.ifpb.project.denguemaps.pdmuserms.entity.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CidadaoCriarDTO {
    @NotBlank
    private String primeiroNome;
    @NotBlank
    private String ultimoNome;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String senha;
    @NotBlank
    private String nome;
    @NotBlank @Size(min = 11, max = 11)
    private String cpf;
    @NotBlank
    private Endereco endereco;
    @NotBlank
    private UUID fkMunicipioID;

}
