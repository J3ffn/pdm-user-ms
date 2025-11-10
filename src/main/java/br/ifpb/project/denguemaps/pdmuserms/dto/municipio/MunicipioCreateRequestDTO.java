package br.ifpb.project.denguemaps.pdmuserms.dto.municipio;


import br.ifpb.project.denguemaps.pdmuserms.enums.Estado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MunicipioCreateRequestDTO {
    @NotBlank
    @Size(min = 3, max = 255)
    private String nome;
    private String geolocalizacao;
    @NotBlank
    private Estado estado;
    private String fkSecretariaId;
}
