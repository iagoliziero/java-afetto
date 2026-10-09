package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.avaliacao.AvaliacaoRequest;
import br.com.fiap.java_afetto.dto.avaliacao.AvaliacaoResponse;
import br.com.fiap.java_afetto.mapper.AvaliacaoMapper;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.clinica.Avaliacao;
import br.com.fiap.java_afetto.model.clinica.Clinica;
import br.com.fiap.java_afetto.repository.AvaliacaoRepository;
import br.com.fiap.java_afetto.repository.ClinicaRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Avaliações das clínicas. Cada usuário tem no máximo uma por clínica:
 * avaliar de novo atualiza a nota e o comentário em vez de duplicar.
 */
@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ClinicaRepository clinicaRepository;
    private final AvaliacaoMapper avaliacaoMapper;
    private final UsuarioLogadoService usuarioLogadoService;
    private final Clock relogio;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            ClinicaRepository clinicaRepository,
            AvaliacaoMapper avaliacaoMapper,
            UsuarioLogadoService usuarioLogadoService,
            Clock relogio
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.clinicaRepository = clinicaRepository;
        this.avaliacaoMapper = avaliacaoMapper;
        this.usuarioLogadoService = usuarioLogadoService;
        this.relogio = relogio;
    }

    public AvaliacaoResponse avaliar(UUID idClinica, AvaliacaoRequest request) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Clinica clinica = buscarClinica(idClinica);
        LocalDateTime agora = LocalDateTime.now(relogio);

        Avaliacao avaliacao = avaliacaoRepository
                .findByUsuario_IdAndClinica_Id(usuario.getId(), clinica.getId())
                .orElse(null);

        if (avaliacao == null) {
            avaliacao = new Avaliacao();
            avaliacao.setUsuario(usuario);
            avaliacao.setClinica(clinica);
            avaliacao.setDataCriacao(agora);
        } else {
            avaliacao.setDataAtualizacao(agora);
        }

        avaliacao.setNota(request.nota());
        avaliacao.setComentario(
                request.comentario() == null || request.comentario().isBlank()
                        ? null
                        : request.comentario().trim()
        );

        return avaliacaoMapper.avaliacaoToResponse(
                avaliacaoRepository.save(avaliacao),
                true
        );
    }

    public Page<AvaliacaoResponse> read(UUID idClinica, Pageable pageable) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Clinica clinica = buscarClinica(idClinica);

        return avaliacaoRepository
                .findByClinica_Id(clinica.getId(), pageable)
                .map(avaliacao -> avaliacaoMapper.avaliacaoToResponse(
                        avaliacao,
                        avaliacao.getUsuario().getId().equals(usuario.getId())
                ));
    }

    // Apaga a avaliação do usuário logado; se ele não tinha, não faz nada
    public void delete(UUID idClinica) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        Clinica clinica = buscarClinica(idClinica);

        avaliacaoRepository
                .findByUsuario_IdAndClinica_Id(usuario.getId(), clinica.getId())
                .ifPresent(avaliacaoRepository::delete);
    }

    private Clinica buscarClinica(UUID id) {

        return clinicaRepository
                .findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Clínica não encontrada"
                        )
                );
    }
}
