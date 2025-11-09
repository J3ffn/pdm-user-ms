package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Cidadao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CidadaoRepository extends JpaRepository<Cidadao, UUID> {}