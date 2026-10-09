package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.remedio.RemedioLista;
import br.com.fiap.java_afetto.dto.remedio.RemedioRequest;
import br.com.fiap.java_afetto.dto.remedio.RemedioResponse;
import br.com.fiap.java_afetto.exception.RegraNegocioException;
import br.com.fiap.java_afetto.mapper.RemedioMapper;
import br.com.fiap.java_afetto.model.Remedio;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.pet.Pet;
import br.com.fiap.java_afetto.repository.RemedioRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RemedioService {

    private final RemedioRepository remedioRepository;
    private final RemedioMapper remedioMapper;
    private final UsuarioLogadoService usuarioLogadoService;

    public RemedioService(
            RemedioRepository remedioRepository,
            RemedioMapper remedioMapper,
            UsuarioLogadoService usuarioLogadoService
    ) {
        this.remedioRepository = remedioRepository;
        this.remedioMapper = remedioMapper;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public RemedioResponse create(RemedioRequest request) {

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(request.idPet());

        validarPeriodo(request);

        Remedio remedio = new Remedio();

        remedio.setNomeRemedio(request.nomeRemedio());
        remedio.setDosagem(request.dosagem());
        remedio.setFrequencia(request.frequencia());
        remedio.setDataInicio(request.dataInicio());
        remedio.setDataFim(request.dataFim());
        remedio.setObservacoes(request.observacoes());
        remedio.setPet(pet);

        return remedioMapper.remedioToResponse(
                remedioRepository.save(remedio)
        );
    }

    public RemedioResponse read(UUID id) {

        return remedioMapper.remedioToResponse(
                buscarRemedioDoUsuario(id)
        );
    }

    // Só os remédios dos pets do usuário logado
    public Page<RemedioLista> read(Pageable pageable) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        return remedioRepository
                .findByPet_Usuario_Id(usuario.getId(), pageable)
                .map(remedioMapper::remedioToResponseLista);
    }

    public Page<RemedioLista> readPorPet(UUID idPet, Pageable pageable) {

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(idPet);

        return remedioRepository
                .findByPet_Id(pet.getId(), pageable)
                .map(remedioMapper::remedioToResponseLista);
    }

    public RemedioResponse update(
            UUID id,
            RemedioRequest request
    ) {

        Remedio remedio = buscarRemedioDoUsuario(id);

        Pet pet = usuarioLogadoService.buscarPetDoUsuario(request.idPet());

        validarPeriodo(request);

        remedio.setNomeRemedio(request.nomeRemedio());
        remedio.setDosagem(request.dosagem());
        remedio.setFrequencia(request.frequencia());
        remedio.setDataInicio(request.dataInicio());
        remedio.setDataFim(request.dataFim());
        remedio.setObservacoes(request.observacoes());
        remedio.setPet(pet);

        return remedioMapper.remedioToResponse(
                remedioRepository.save(remedio)
        );
    }

    public void delete(UUID id) {

        Remedio remedio = buscarRemedioDoUsuario(id);

        remedioRepository.delete(remedio);
    }

    // Remédio de pet de outro usuário responde como "não encontrado"
    private Remedio buscarRemedioDoUsuario(UUID id) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        return remedioRepository
                .findByIdAndPet_Usuario_Id(id, usuario.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Remédio não encontrado"
                        )
                );
    }

    private void validarPeriodo(RemedioRequest request) {

        if (request.dataFim() != null
                && request.dataFim().isBefore(request.dataInicio())) {

            throw new RegraNegocioException(
                    "A data de fim não pode ser antes da data de início"
            );
        }
    }
}
