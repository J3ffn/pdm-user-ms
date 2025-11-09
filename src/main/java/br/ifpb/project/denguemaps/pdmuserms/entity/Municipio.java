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
@Table(name = "municipio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Municipio {

    @Id
    @Column(name = "municipio_id")
    private UUID id;

    @Column(nullable = false)
    private String nome;

    private String geolocalizacao;

    @Column(nullable = false, length = 2)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_secretaria_id", nullable = false)
    private Secretaria secretaria;
}
