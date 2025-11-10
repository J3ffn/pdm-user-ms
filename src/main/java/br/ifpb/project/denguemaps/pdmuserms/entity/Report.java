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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @Column(name = "report_id")
    private UUID id;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String coordenadas;

    @Column(name = "classificacao_risco")
    private String classificacaoRisco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cidadao_id", nullable = true)
    private Cidadao cidadao;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_by", nullable = false)
    private OffsetDateTime updatedBy;
}