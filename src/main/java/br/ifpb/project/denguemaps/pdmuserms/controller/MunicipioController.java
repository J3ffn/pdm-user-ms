package br.ifpb.project.denguemaps.pdmuserms.controller;

import br.ifpb.project.denguemaps.pdmuserms.dto.municipio.MunicipioResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.service.MunicipioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/municipio")
@RequiredArgsConstructor
public class MunicipioController {

    private final MunicipioService municipioService;

    @GetMapping
    public ResponseEntity<List<MunicipioResponseDTO>> buscarMunicipios() {
        var municipios = municipioService.buscarTodosMunicipios();

        return ResponseEntity.ok(municipios);
    }

}
