package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.convite.CreateConviteRequest;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.RegistrarServidorRequest;
import br.ifpb.project.denguemaps.pdmuserms.entity.Convite;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.entity.Servidor;
import br.ifpb.project.denguemaps.pdmuserms.repository.ConviteRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.SecretariaRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.ServidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final SecretariaRepository secretariaRepository;
    private final MunicipioRepository municipioRepository;
    private final ServidorRepository servidorRepository;

    @Transactional
    public Convite criarConvite(CreateConviteRequest dto, Servidor criador) {
        Secretaria secretaria = secretariaRepository.findById(dto.secretariaId())
                .orElseThrow(() -> new IllegalArgumentException("Secretaria não encontrada"));
        Municipio municipio = municipioRepository.findById(dto.municipioId())
                .orElseThrow(() -> new IllegalArgumentException("Município não encontrado"));

        Convite convite = new Convite();

        convite.setToken(UUID.randomUUID().toString());
        convite.setSecretaria(secretaria);
        convite.setMunicipio(municipio);
        convite.setServidorCriador(criador);
        convite.setPerfilDestino(dto.perfilDestino() != null ? dto.perfilDestino() : "AGENTE_SANITARIO");
        convite.setExpiresAt(OffsetDateTime.now().plusDays(2));
        convite.setCreatedAt(OffsetDateTime.now());

        return conviteRepository.save(convite);
    }

    @Transactional
    public Servidor registrarServidor(String token, RegistrarServidorRequest req) {
        Convite convite = conviteRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Convite inválido"));

        if (convite.getUsedAt() != null) {
            throw new IllegalStateException("Convite já utilizado");
        }
        if (OffsetDateTime.now().isAfter(convite.getExpiresAt())) {
            throw new IllegalStateException("Convite expirado");
        }

        Servidor servidor = new Servidor();
        servidor.setId(UUID.randomUUID());
        servidor.setNome(req.nome());
        servidor.setCpf(req.cpf());
        servidor.setRg(req.rg());
        servidor.setRefKeycloakId(req.refKeycloakId());
        servidor.setSecretaria(convite.getSecretaria());
        servidor.setMunicipio(convite.getMunicipio());

        servidorRepository.save(servidor);

        convite.setUsedAt(OffsetDateTime.now());
        conviteRepository.save(convite);

        return servidor;
    }
}