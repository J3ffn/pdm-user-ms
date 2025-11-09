package br.ifpb.project.denguemaps.pdmuserms.dto.convite;

import java.util.UUID;

public record CreateConviteRequest(
        UUID secretariaId,
        UUID municipioId,
        String perfilDestino
) {}