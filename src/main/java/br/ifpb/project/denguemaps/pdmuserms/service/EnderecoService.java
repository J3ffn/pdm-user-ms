package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.endereco.EnderecoResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.endereco.EnderecoCreateRequestDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Endereco;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.repository.EnderecoRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnderecoService {
    private final EnderecoRepository enderecoRepository;
    private final MunicipioRepository municipioRepository;

    public EnderecoResponseDTO registrarEndereco(EnderecoCreateRequestDTO enderecoCreateRequestDTO){
        Municipio municipio;
        Endereco endereco = returnEntity(enderecoCreateRequestDTO);
        try{
            // Buscar municipio
            municipio = municipioRepository.findById(
                            UUID.fromString(enderecoCreateRequestDTO.getFkMunicipioId()))
                    .orElseThrow(() -> new IllegalArgumentException("Municipio não encontrado"));
            // encontrado, associar ao endereco:
            endereco.setMunicipio(municipio);
        }catch (IllegalArgumentException ignored){
            // Municipio nao encontrado, continuar a criar endereco sem associacao com municipio.
        }

        endereco = saveEntity(endereco);
        return returnReponse(endereco);

    }

    private Endereco saveEntity(Endereco endereco){
        return enderecoRepository.save(endereco);
    }
    private Endereco returnEntity(EnderecoCreateRequestDTO enderecoCreateRequestDTO){
        Endereco endereco = new Endereco();
        endereco.setLogradouro(enderecoCreateRequestDTO.getLogradouro());
        endereco.setCep(enderecoCreateRequestDTO.getCep());
        endereco.setNumero(enderecoCreateRequestDTO.getNumero());
        return endereco;
    }
    private EnderecoResponseDTO returnReponse(Endereco endereco){
        EnderecoResponseDTO enderecoResponseDTO = new EnderecoResponseDTO();
        enderecoResponseDTO.setLogradouro(endereco.getLogradouro());
        enderecoResponseDTO.setCep(endereco.getCep());
        enderecoResponseDTO.setNumero(endereco.getNumero());
        return enderecoResponseDTO;
    }
}
