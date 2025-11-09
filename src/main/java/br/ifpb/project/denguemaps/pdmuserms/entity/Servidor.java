package br.ifpb.project.denguemaps.pdmuserms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "servidor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servidor {

    @Id
    @Column(name = "servidor_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_secretaria_id")
    private Secretaria secretaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_municipio_id")
    private Municipio municipio;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 9)
    private String rg;

    @Column(name = "ref_keycloak_id", nullable = false)
    private UUID refKeycloakId;
}