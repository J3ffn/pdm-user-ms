package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Convite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConviteRepository extends JpaRepository<Convite, UUID> {
    Optional<Convite> findByToken(String token);
}
