package br.com.fiap.java_afetto.repository;

import br.com.fiap.java_afetto.model.Historico;
import br.com.fiap.java_afetto.model.StatusHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistoricoRepository extends JpaRepository<Historico, UUID> {

    Page<Historico> findByPet_Id(UUID idPet, Pageable pageable);

    Page<Historico> findByPet_Usuario_Id(UUID idUsuario, Pageable pageable);

    List<Historico> findByPet_Usuario_Id(UUID idUsuario);

    Optional<Historico> findByIdAndPet_Usuario_Id(UUID id, UUID idUsuario);

    void deleteByPet_Id(UUID idPet);

    // Horários já ocupados numa clínica (consulta cancelada libera o horário)
    List<Historico> findByClinica_IdAndDataBetweenAndStatusNot(
            UUID idClinica, LocalDate inicio, LocalDate fim, StatusHistorico status);

    List<Historico> findByClinica_IdAndDataAndHoraAndStatusNot(
            UUID idClinica, LocalDate data, LocalTime hora, StatusHistorico status);
}
