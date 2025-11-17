package br.ifpb.project.denguemaps.pdmuserms.dto.questionario;

import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Cidadao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionarioResponseDTO {
    private UUID id;
    private String perguntas;
    private String respostas;
    private UUID fkCidadaoId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedBy;
}
