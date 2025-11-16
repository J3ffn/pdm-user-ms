package br.ifpb.project.denguemaps.pdmuserms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportUpdateDTO extends ReportCriacaoDTO{

    private UUID id;
}
