package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.municipio.MunicipioCreateRequestDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.municipio.MunicipioResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.enums.Estado;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.SecretariaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MunicipioService {
    private final MunicipioRepository municipioRepository;
    private final SecretariaRepository secretariaRepository;
    private final ObjectMapper objectMapper;

    public MunicipioResponseDTO registrarMunicipio(MunicipioCreateRequestDTO municipioCreateRequestDTO) {
        Secretaria secretaria;
        Municipio municipio = returnEntity(municipioCreateRequestDTO);
        try {
            secretaria = secretariaRepository.findById(UUID.fromString(municipioCreateRequestDTO.getFkSecretariaId())).orElseThrow(() -> new IllegalArgumentException("Secretaria não encontrada"));
            municipio.setSecretaria(secretaria);
        } catch (IllegalArgumentException ignored) {
            // Nao existe secretaria informada, continua a criar o municipio.
        }
        municipio = saveEntity(municipio);
        return returnResponse(municipio);

    }

    private Municipio saveEntity(Municipio municipio) {
        return municipioRepository.save(municipio);
    }

    public List<MunicipioResponseDTO> buscarTodosMunicipios() {
        List<Municipio> municipios = municipioRepository.findAll();
        return municipios
                .stream()
                .map(
                        municipio -> objectMapper.convertValue(municipio, MunicipioResponseDTO.class)
                ).toList();
    }

    public MunicipioResponseDTO buscarMunicipioNomeEstado(String nome, Estado estado) {
        Municipio municipio = municipioRepository.findByNomeAndEstado(nome, estado).orElseThrow(() -> new IllegalArgumentException("Municipiuo não encontrado"));
        return returnResponse(municipio);
    }

    private Municipio returnEntity(MunicipioCreateRequestDTO municipioCreateRequestDTO) {
        Municipio municipio = new Municipio();
        municipio.setNome(municipioCreateRequestDTO.getNome());
        municipio.setEstado(municipioCreateRequestDTO.getEstado());
        municipio.setGeolocalizacao(municipioCreateRequestDTO.getGeolocalizacao());
        return municipio;
    }

    private MunicipioResponseDTO returnResponse(Municipio municipio) {
        MunicipioResponseDTO municipioResponseDTO = new MunicipioResponseDTO();
        municipioResponseDTO.setNome(municipio.getNome());
        municipioResponseDTO.setGeolocalizacao(municipio.getGeolocalizacao());
        municipioResponseDTO.setEstado(municipio.getEstado());
        return municipioResponseDTO;
    }
}
