package br.ifpb.project.denguemaps.pdmuserms.dto.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoCreateRequestDTO {
    @NotBlank
    private String logradouro;
    @NotBlank
    @Size(min = 8, max = 8)
    private String cep;
    @NotBlank
    private  String numero;
    private String fkMunicipioId;
}
