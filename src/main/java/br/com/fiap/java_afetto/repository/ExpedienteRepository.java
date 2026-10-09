package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.clinica.Expediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpedienteRepository extends JpaRepository<Expediente, UUID> {

    List<Expediente> findByClinica_Id(UUID idClinica);

    Optional<Expediente> findByClinica_IdAndDiaSemana(UUID idClinica, DayOfWeek diaSemana);
}
