package br.ifpb.project.denguemaps.pdmuserms.dto.secretaria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecretariaResponseDTO {

    private UUID id;
    private String nome;
    private UUID enderecoId;
    private UUID gestorId;
}
