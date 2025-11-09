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
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Id
    @Column(name = "endereco_id")
    private UUID id;

    private String logradouro;

    private String cep;

    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_municipio_id", nullable = false)
    private Municipio municipio;
}
