package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.clinica.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicaRepository extends JpaRepository<Clinica, UUID> {

    List<Clinica> findByNomeContainingIgnoreCase(String nome);
}
