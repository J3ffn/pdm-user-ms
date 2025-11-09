package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Servidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServidorRepository extends JpaRepository<Servidor, UUID> {
    Optional<Servidor> findByRefKeycloakId(UUID refKeycloakId);
}