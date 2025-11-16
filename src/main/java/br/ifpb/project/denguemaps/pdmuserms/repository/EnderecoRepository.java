package br.ifpb.project.denguemaps.pdmuserms.repository;

import br.ifpb.project.denguemaps.pdmuserms.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnderecoRepository extends JpaRepository<Endereco, UUID> {
    public Endereco findAllByMunicipioIdAndLogradouroAndNumeroAndCep(UUID uuid, String logradouro, String Numero, String cep);
}