package br.ifpb.project.denguemaps.pdmuserms.controller;


import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportCriacaoDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportUpdateDTO;
import br.ifpb.project.denguemaps.pdmuserms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDTO> registrarReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportCriacaoDTO request
    ) {
        ReportResponseDTO reportResponseDTO = reportService.cadastrarReport(request);
        return new ResponseEntity<>(reportResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ReportResponseDTO> atualizarReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportUpdateDTO request
    ) {
        ReportResponseDTO reportResponseDTO = reportService.atualizarReport(request);
        return new ResponseEntity<>(reportResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportResponseDTO>> listarTodoReport(
            @AuthenticationPrincipal Jwt jwt
    ){
        List<ReportResponseDTO> listaReport = reportService.buscarTodoReport();
        return new ResponseEntity<>(listaReport, HttpStatus.OK);
    }


}
