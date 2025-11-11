package br.ifpb.project.denguemaps.pdmuserms.controller;

import br.ifpb.project.denguemaps.pdmuserms.dto.secretaria.SecretariaResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.service.SecretariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/secretaria")
@RequiredArgsConstructor
public class SecretariaController {

    private final SecretariaService municipioService;

    @GetMapping
    public ResponseEntity<List<SecretariaResponseDTO>> buscarMunicipios() {
        var secretarias = municipioService.buscarTodasSecretarias();

        return ResponseEntity.ok(secretarias);
    }

}
