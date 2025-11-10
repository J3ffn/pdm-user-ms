package br.ifpb.project.denguemaps.pdmuserms.dto.municipio;

import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.enums.Estado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MunicipioResponseDTO {
    private String nome;
    private String geolocalizacao;
    private Estado estado;
    private Secretaria secretaria;
}
