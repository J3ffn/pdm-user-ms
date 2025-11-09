package br.ifpb.project.denguemaps.pdmuserms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "convite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Convite {

    @Id
    @Column(name = "convite_id")
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_secretaria_id", nullable = false)
    private Secretaria secretaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_municipio_id", nullable = false)
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_servidor_criador_id", nullable = false)
    private Servidor servidorCriador;

    @Column(name = "perfil_destino", nullable = false)
    private String perfilDestino = "AGENTE_SANITARIO";

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
