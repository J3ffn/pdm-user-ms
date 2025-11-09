package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MunicipioRepository extends JpaRepository<Municipio, UUID> {}