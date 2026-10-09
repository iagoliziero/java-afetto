package br.com.fiap.java_afetto.service;

import br.com.fiap.java_afetto.dto.perfil.EnderecoPerfilRequest;
import br.com.fiap.java_afetto.dto.perfil.PerfilRequest;
import br.com.fiap.java_afetto.dto.perfil.PerfilResponse;
import br.com.fiap.java_afetto.exception.RegraNegocioException;
import br.com.fiap.java_afetto.mapper.PerfilMapper;
import br.com.fiap.java_afetto.model.Usuario;
import br.com.fiap.java_afetto.model.endereco.Bairro;
import br.com.fiap.java_afetto.model.endereco.Cidade;
import br.com.fiap.java_afetto.model.endereco.Endereco;
import br.com.fiap.java_afetto.model.endereco.Estado;
import br.com.fiap.java_afetto.model.endereco.Logradouro;
import br.com.fiap.java_afetto.model.endereco.Pais;
import br.com.fiap.java_afetto.repository.PetRepository;
import br.com.fiap.java_afetto.repository.UsuarioRepository;
import br.com.fiap.java_afetto.repository.endereco.BairroRepository;
import br.com.fiap.java_afetto.repository.endereco.CidadeRepository;
import br.com.fiap.java_afetto.repository.endereco.EnderecoRepository;
import br.com.fiap.java_afetto.repository.endereco.EstadoRepository;
import br.com.fiap.java_afetto.repository.endereco.LogradouroRepository;
import br.com.fiap.java_afetto.repository.endereco.PaisRepository;
import br.com.fiap.java_afetto.security.UsuarioLogadoService;
import br.com.fiap.java_afetto.util.EstadosBrasil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Finalize seu cadastro": moradia, tela de proteção, quantidade de pets e
 * endereço do usuário logado.
 *
 * O endereço chega com os campos do formulário (CEP, logradouro, bairro,
 * cidade, UF) e é guardado na hierarquia que já existia no banco
 * (País > Estado > Cidade > Bairro > Logradouro > Endereço): cada nível é
 * reaproveitado se já existir, ou criado. As coordenadas vêm do CEP.
 */
@Service
public class PerfilService {

    private final UsuarioLogadoService usuarioLogadoService;
    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;
    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;
    private final BairroRepository bairroRepository;
    private final LogradouroRepository logradouroRepository;
    private final EnderecoRepository enderecoRepository;
    private final GeolocalizacaoService geolocalizacaoService;
    private final PerfilMapper perfilMapper;

    public PerfilService(
            UsuarioLogadoService usuarioLogadoService,
            UsuarioRepository usuarioRepository,
            PetRepository petRepository,
            PaisRepository paisRepository,
            EstadoRepository estadoRepository,
            CidadeRepository cidadeRepository,
            BairroRepository bairroRepository,
            LogradouroRepository logradouroRepository,
            EnderecoRepository enderecoRepository,
            GeolocalizacaoService geolocalizacaoService,
            PerfilMapper perfilMapper
    ) {
        this.usuarioLogadoService = usuarioLogadoService;
        this.usuarioRepository = usuarioRepository;
        this.petRepository = petRepository;
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.cidadeRepository = cidadeRepository;
        this.bairroRepository = bairroRepository;
        this.logradouroRepository = logradouroRepository;
        this.enderecoRepository = enderecoRepository;
        this.geolocalizacaoService = geolocalizacaoService;
        this.perfilMapper = perfilMapper;
    }

    public PerfilResponse read() {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();

        return perfilMapper.perfilToResponse(
                usuario,
                petRepository.countByUsuario_Id(usuario.getId())
        );
    }

    @Transactional
    public PerfilResponse update(PerfilRequest request) {

        Usuario usuario = usuarioLogadoService.buscarUsuarioLogado();
        EnderecoPerfilRequest dados = request.endereco();

        String uf = dados.estado().trim().toUpperCase();
        String nomeDoEstado = EstadosBrasil.nome(uf)
                .orElseThrow(() -> new RegraNegocioException("UF inválida: " + dados.estado()));

        Logradouro logradouro = logradouro(dados.logradouro(), bairro(dados.bairro(), cidade(dados.cidade(), estado(uf, nomeDoEstado))));

        // Atualiza o endereço que o usuário já tinha, ou cria o primeiro
        Endereco endereco = usuario.getEndereco() != null ? usuario.getEndereco() : new Endereco();
        String cep = cepFormatado(dados.cep());

        endereco.setCep(cep);
        endereco.setNumero(dados.numero().trim());
        endereco.setComplemento(dados.complemento() == null || dados.complemento().isBlank() ? null : dados.complemento().trim());
        endereco.setLogradouro(logradouro);

        GeolocalizacaoService.Coordenadas coordenadas = geolocalizacaoService.buscarPorCep(cep).orElse(null);
        endereco.setLatitude(coordenadas != null ? coordenadas.latitude() : null);
        endereco.setLongitude(coordenadas != null ? coordenadas.longitude() : null);

        usuario.setEndereco(enderecoRepository.save(endereco));
        usuario.setTipoMoradia(request.tipoMoradia());
        usuario.setTelaProtecao(request.telaProtecao());
        usuario.setQuantidadePets(request.quantidadePets());

        Usuario salvo = usuarioRepository.save(usuario);

        return perfilMapper.perfilToResponse(
                salvo,
                petRepository.countByUsuario_Id(salvo.getId())
        );
    }

    private Estado estado(String uf, String nome) {

        Pais brasil = paisRepository.findFirstBySiglaIgnoreCase("BR").orElseGet(() -> {
            Pais pais = new Pais();
            pais.setNome("Brasil");
            pais.setSigla("BR");
            return paisRepository.save(pais);
        });

        return estadoRepository.findFirstBySiglaIgnoreCaseAndPais_Id(uf, brasil.getId()).orElseGet(() -> {
            Estado estado = new Estado();
            estado.setNome(nome);
            estado.setSigla(uf);
            estado.setPais(brasil);
            return estadoRepository.save(estado);
        });
    }

    private Cidade cidade(String nome, Estado estado) {

        String nomeLimpo = nome.trim();

        return cidadeRepository.findFirstByNomeIgnoreCaseAndEstado_Id(nomeLimpo, estado.getId()).orElseGet(() -> {
            Cidade cidade = new Cidade();
            cidade.setNome(nomeLimpo);
            cidade.setEstado(estado);
            return cidadeRepository.save(cidade);
        });
    }

    private Bairro bairro(String nome, Cidade cidade) {

        String nomeLimpo = nome.trim();

        return bairroRepository.findFirstByNomeIgnoreCaseAndCidade_Id(nomeLimpo, cidade.getId()).orElseGet(() -> {
            Bairro bairro = new Bairro();
            bairro.setNome(nomeLimpo);
            bairro.setCidade(cidade);
            return bairroRepository.save(bairro);
        });
    }

    private Logradouro logradouro(String nome, Bairro bairro) {

        String nomeLimpo = nome.trim();

        return logradouroRepository.findFirstByNomeIgnoreCaseAndBairro_Id(nomeLimpo, bairro.getId()).orElseGet(() -> {
            Logradouro logradouro = new Logradouro();
            logradouro.setNome(nomeLimpo);
            logradouro.setBairro(bairro);
            return logradouroRepository.save(logradouro);
        });
    }

    // "01001000" ou "01001-000" -> "01001-000"
    private String cepFormatado(String cep) {
        String digitos = cep.replaceAll("\\D", "");
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }
}
