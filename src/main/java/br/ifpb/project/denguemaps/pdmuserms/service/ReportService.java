package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportCriacaoDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportUpdateDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmuserms.entity.Report;
import br.ifpb.project.denguemaps.pdmuserms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final CidadaoRepository cidadaoRepository;

    
    public ReportResponseDTO cadastrarReport(ReportCriacaoDTO reportCriacaoDTO) {
        Report report = formatarReportRetornar(reportCriacaoDTO);
        report.setCreatedAt(OffsetDateTime.now());
        return retornarResponse(
                salvarEntidadeRetornar(report)
        );
    }


    public ReportResponseDTO atualizarReport(ReportUpdateDTO reportUpdateDTO){
        Report report = buscarReport(reportUpdateDTO.getId());
        atualizarReport(report, reportUpdateDTO);
        return retornarResponse(salvarEntidadeRetornar(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> buscarTodoReport() {
        List<Report> reports = reportRepository.findAll();
        return mapearReportsResponseDTO(reports);
    }



    // Metodos auxiliares:
    private Report salvarEntidadeRetornar(Report report){
        return reportRepository.save(report);
    }
    private Report formatarReportRetornar(ReportCriacaoDTO reportCriacaoDTO){
        Report report = new Report();
        report.setCidadao(buscarCidadao(reportCriacaoDTO.getFkCidadaoID()));
        report.setCoordenadas(reportCriacaoDTO.getCoordenadas());
        report.setClassificacaoRisco(reportCriacaoDTO.getClassificacaoRisco());
        return report;
    }

    private void atualizarReport(Report report, ReportUpdateDTO reportUpdateDTO){
        report.setCoordenadas(reportUpdateDTO.getCoordenadas());
        report.setClassificacaoRisco(reportUpdateDTO.getClassificacaoRisco());
        report.setUpdatedBy(OffsetDateTime.now());
        report.setCidadao(buscarCidadao(reportUpdateDTO.getFkCidadaoID()));
    }

    private ReportResponseDTO retornarResponse(Report report){
        ReportResponseDTO reportResponseDTO = new ReportResponseDTO();
        reportResponseDTO.setNomeCidadao(report.getCidadao().getNome());
        reportResponseDTO.setId(report.getId());
        reportResponseDTO.setClassificacaoRisco(report.getClassificacaoRisco());
        reportResponseDTO.setCoordenadas(report.getCoordenadas());
        reportResponseDTO.setUpdatedBy(report.getUpdatedBy());
        reportResponseDTO.setCreatedAt(report.getCreatedAt());
        return reportResponseDTO;
    }

    private Cidadao buscarCidadao(UUID uuid){
        return cidadaoRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cidadao não encontrado"));
    }

    private Report buscarReport(UUID uuid){
        return reportRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Report não encontrado"));
    }

    private List<ReportResponseDTO> mapearReportsResponseDTO(List<Report> reports) {
        return reports.stream()
                .map(this::retornarResponse)
                .collect(Collectors.toList());
    }
}
