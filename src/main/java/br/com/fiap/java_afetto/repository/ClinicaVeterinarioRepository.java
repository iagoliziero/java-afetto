package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.clinica.ClinicaVeterinario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicaVeterinarioRepository extends JpaRepository<ClinicaVeterinario, UUID> {

    List<ClinicaVeterinario> findByClinica_IdAndAtivoTrue(UUID idClinica);
}
