package br.ifpb.project.denguemaps.pdmuserms.dto.report;

import br.ifpb.project.denguemaps.pdmuserms.entity.Cidadao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportResponseDTO {
    private UUID id;
    private String coordenadas;
    private String classificacaoRisco;
    private String nomeCidadao;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedBy;
}
