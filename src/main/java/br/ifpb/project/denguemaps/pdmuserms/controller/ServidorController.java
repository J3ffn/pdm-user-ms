package br.ifpb.project.denguemaps.pdmuserms.controller;

import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorUpdateRequestDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorRegistrationResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.ServidorUpdateResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.service.ServidorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/servidor", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class ServidorController {

    private final ServidorService servidorService;

    @PutMapping()
    public ResponseEntity<ServidorUpdateResponseDTO> updateServidor(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody @Valid ServidorUpdateRequestDTO updateRequestDTO) {

        String adminToken = authHeader.replace("Bearer ", "");

        ServidorUpdateResponseDTO responseDTO = servidorService.updateServidor(updateRequestDTO, adminToken);

        // Retorna 200 OK
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<ServidorRegistrationResponseDTO> getServidorByCpf(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String cpf) {

        String adminToken = authHeader.replace("Bearer ", "");

        // 🚨 Passando o token para a Service
        ServidorRegistrationResponseDTO responseDTO = servidorService.getServidorByCpf(cpf, adminToken);

        return ResponseEntity.ok(responseDTO);
    }


}
