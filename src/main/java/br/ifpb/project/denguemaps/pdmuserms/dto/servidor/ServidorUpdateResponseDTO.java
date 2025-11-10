package br.ifpb.project.denguemaps.pdmuserms.dto.servidor;

import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;

public record ServidorUpdateResponseDTO(
        // Retorna todos os dados atualizados
        String firstName,
        String lastName,
        String email,
        String nome,
        String rg,
        Secretaria secretaria,
        Municipio municipio
) { }
