package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {}