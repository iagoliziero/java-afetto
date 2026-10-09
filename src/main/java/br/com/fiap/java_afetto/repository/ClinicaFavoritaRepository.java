package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.clinica.ClinicaFavorita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicaFavoritaRepository extends JpaRepository<ClinicaFavorita, UUID> {

    List<ClinicaFavorita> findByUsuario_Id(UUID idUsuario);

    boolean existsByUsuario_IdAndClinica_Id(UUID idUsuario, UUID idClinica);

    Optional<ClinicaFavorita> findByUsuario_IdAndClinica_Id(UUID idUsuario, UUID idClinica);
}
