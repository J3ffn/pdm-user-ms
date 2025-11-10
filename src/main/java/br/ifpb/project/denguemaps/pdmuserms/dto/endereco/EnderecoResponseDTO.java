package br.ifpb.project.denguemaps.pdmuserms.dto.endereco;

import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoResponseDTO {
    private String logradouro;
    private String cep;
    private  String numero;
    private Municipio municipio;
}
