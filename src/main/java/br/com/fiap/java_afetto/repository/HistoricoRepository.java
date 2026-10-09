package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.pet.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HistoricoRepository extends JpaRepository<Historico, UUID> {

    Page<Historico> findByPet_Id(UUID idPet, Pageable pageable);

    Page<Historico> findByPet_Usuario_Id(UUID idUsuario, Pageable pageable);

    Optional<Historico> findByIdAndPet_Usuario_Id(UUID id, UUID idUsuario);

    void deleteByPet_Id(UUID idPet);
}
