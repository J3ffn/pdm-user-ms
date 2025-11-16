package br.ifpb.project.denguemaps.pdmuserms.controller;

import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoAtualizarDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoCriarDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.service.CidadaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/cidadao")
public class CidadaoController {
    private final CidadaoService cidadaoService;
    @PostMapping
    public ResponseEntity<CidadaoResponseDTO> registrarCidadao(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CidadaoCriarDTO cidadaoCriarDTO
            ){
        return new ResponseEntity<>(cidadaoService.registrarCidadao(cidadaoCriarDTO), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<CidadaoResponseDTO> atualizarCidadao(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody @Valid CidadaoAtualizarDTO cidadaoAtualizarDTO,
            @RequestParam UUID uuid
            ){
        String token = authHeader.replace("Bearer ", "");
        return new ResponseEntity<>(cidadaoService.atualizarCidadao(cidadaoAtualizarDTO, uuid, token), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<String> deletarReport( @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                @RequestParam UUID uuid
    ){
        String token = authHeader.replace("Bearer ", "");
        cidadaoService.deletarCidadao(uuid, token);
        return new ResponseEntity<String>("", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/esp")
    public ResponseEntity<CidadaoResponseDTO> buscarReportEspecifico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam UUID uuid
    ){
        String token = authHeader.replace("Bearer ", "");
        CidadaoResponseDTO cidadaoResponseDTO = cidadaoService.buscarCidadaoEspecificoId(uuid, token);
        return new ResponseEntity<>(cidadaoResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CidadaoResponseDTO>> listarTodoCidadao(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ){
        String token = authHeader.replace("Bearer ", "");
        List<CidadaoResponseDTO> listaCidadao = cidadaoService.buscarTodoCidadao(token);
        return new ResponseEntity<>(listaCidadao, HttpStatus.OK);
    }

}