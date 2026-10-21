package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.usuario.UsuarioAtualizacaoRequest;
import br.com.fiap.java_afetto.dto.usuario.UsuarioLista;
import br.com.fiap.java_afetto.dto.usuario.UsuarioRequest;
import br.com.fiap.java_afetto.dto.usuario.UsuarioResponse;
import br.com.fiap.java_afetto.dto.usuario.UsuarioRoleRequest;
import br.com.fiap.java_afetto.exception.ConflitoException;
import br.com.fiap.java_afetto.mapper.UsuarioMapper;
import br.com.fiap.java_afetto.model.UserRole;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.endereco.Endereco;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import br.com.fiap.java_afetto.repository.endereco.EnderecoRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioLogadoService usuarioLogadoService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            EnderecoRepository enderecoRepository,
            PasswordEncoder passwordEncoder,
            UsuarioLogadoService usuarioLogadoService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public UsuarioResponse create(UsuarioRequest usuarioRequest) {

        // E-mail repetido: 409 com mensagem (antes estourava no banco e chegava como 403 sem corpo)
        garantirEmailLivre(usuarioRequest.email(), null);

        Usuario usuario = new Usuario();

        usuario.setNome(usuarioRequest.nome());
        usuario.setCpf(usuarioRequest.cpf());
        usuario.setDataNascimento(usuarioRequest.dataNascimento());
        usuario.setEmail(usuarioRequest.email());

        usuario.setSenha(
                passwordEncoder.encode(usuarioRequest.senha())
        );

        usuario.setTelefone(usuarioRequest.telefone());

        usuario.setRole(UserRole.USER);

        if (usuarioRequest.idEndereco() != null) {

            Endereco endereco = enderecoRepository
                    .findById(usuarioRequest.idEndereco())
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Endereço não encontrado"
                            )
                    );

            usuario.setEndereco(endereco);
        }

        return usuarioMapper.usuarioToResponse(
                usuarioRepository.save(usuario)
        );
    }

    // Só a própria conta (o ADMIN vê qualquer uma); outra conta responde 404
    public UsuarioResponse read(UUID id) {

        return usuarioMapper.usuarioToResponse(
                usuarioLogadoService.buscarUsuarioPermitido(id)
        );
    }

    public UsuarioResponse readLogado() {

        return usuarioMapper.usuarioToResponse(
                usuarioLogadoService.buscarUsuarioLogado()
        );
    }

    public Page<UsuarioLista> read(Pageable pageable) {

        return usuarioRepository
                .findAll(pageable)
                .map(usuarioMapper::usuarioToResponseLista);
    }

    // A senha não muda aqui: só em PUT /usuario/me/senha (conferindo a atual).
    // Só a própria conta (o ADMIN edita qualquer uma); outra conta responde 404.
    public UsuarioResponse update(
            UUID id,
            UsuarioAtualizacaoRequest request
    ) {

        Usuario logado = usuarioLogadoService.buscarUsuarioLogado();
        Usuario usuario = usuarioLogadoService.buscarUsuarioPermitido(id);
        boolean propriaConta = usuario.getId().equals(logado.getId());

        garantirEmailLivre(request.email(), usuario.getId());

        usuario.setNome(request.nome());
        usuario.setCpf(request.cpf());
        usuario.setEmail(request.email());
        usuario.setTelefone(request.telefone());
        usuario.setDataNascimento(request.dataNascimento());

        if (request.idEndereco() != null) {

            Endereco endereco = enderecoRepository
                    .findById(request.idEndereco())
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Endereço não encontrado"
                            )
                    );

            usuario.setEndereco(endereco);
        }

        Usuario salvo = usuarioRepository.save(usuario);

        // Trocou o próprio e-mail: a sessão passa a ser do e-mail novo
        if (propriaConta) {
            usuarioLogadoService.atualizarEmailDaSessao(salvo.getEmail());
        }

        return usuarioMapper.usuarioToResponse(salvo);
    }

    public void delete(UUID id) {

        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Usuário não encontrado"
            );
        }

        usuarioRepository.deleteById(id);
    }

    public UsuarioResponse alterarRole(UUID id, UsuarioRoleRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Usuário não encontrado")
                );

        usuario.setRole(request.role());

        return usuarioMapper.usuarioToResponse(
                usuarioRepository.save(usuario)
        );
    }

    /** O e-mail não pode ser de outra conta (maiúsculas e minúsculas contam como iguais). */
    private void garantirEmailLivre(String email, UUID idDono) {

        if (email == null) {
            return;
        }

        usuarioRepository
                .findFirstByEmailIgnoreCase(email.trim())
                .filter(outro -> !outro.getId().equals(idDono))
                .ifPresent(outro -> {
                    throw new ConflitoException("Este e-mail já está cadastrado");
                });
    }
}
