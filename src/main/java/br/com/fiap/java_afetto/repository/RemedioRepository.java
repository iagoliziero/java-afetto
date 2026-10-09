package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.Remedio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RemedioRepository extends JpaRepository<Remedio, UUID> {

    Page<Remedio> findByPet_Id(UUID idPet, Pageable pageable);

    Page<Remedio> findByPet_Usuario_Id(UUID idUsuario, Pageable pageable);

    List<Remedio> findByPet_Usuario_Id(UUID idUsuario);

    Optional<Remedio> findByIdAndPet_Usuario_Id(UUID id, UUID idUsuario);

    void deleteByPet_Id(UUID idPet);
}
