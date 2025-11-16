package br.ifpb.project.denguemaps.pdmuserms.dto.cidadao;

import br.ifpb.project.denguemaps.pdmuserms.entity.Endereco;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CidadaoResponseDTO {
    private UUID id;
    private String primeiroNome;
    private String ultimoNome;
    private String email;
    private String nome;
    private String cpf;
    private UUID fkMunicipioID;
}
