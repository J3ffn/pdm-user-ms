package br.ifpb.project.denguemaps.pdmuserms.dto.convite;

public record CreateConviteResponse(
        String inviteUrl,
        String expiresAt
) {}