package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecretariaRepository extends JpaRepository<Secretaria, UUID> {}