package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.secretaria.SecretariaResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.repository.SecretariaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecretariaService {

    private final SecretariaRepository secretariaRepository;
    private final ObjectMapper objectMapper;

    public List<SecretariaResponseDTO> buscarTodasSecretarias() {
        List<Secretaria> secretarias = secretariaRepository.findAll();
        return secretarias
                .stream()
                .map(
                        secretaria -> objectMapper.convertValue(secretaria, SecretariaResponseDTO.class)
                )
                .toList();
    }

}
