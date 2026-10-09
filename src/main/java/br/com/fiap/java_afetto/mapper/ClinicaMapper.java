package br.com.fiap.java_afetto.mapper;

import br.com.fiap.java_afetto.controller.ClinicaController;
import br.com.fiap.java_afetto.dto.agendamento.DiaDisponivel;
import br.com.fiap.java_afetto.dto.agendamento.HorariosClinicaResponse;
import br.com.fiap.java_afetto.dto.clinica.ClinicaLista;
import br.com.fiap.java_afetto.dto.clinica.ClinicaResponse;
import br.com.fiap.java_afetto.dto.clinica.EnderecoClinica;
import br.com.fiap.java_afetto.dto.clinica.ExpedienteClinica;
import br.com.fiap.java_afetto.dto.clinica.VeterinarioClinica;
import br.com.fiap.java_afetto.model.clinica.Clinica;
import br.com.fiap.java_afetto.model.clinica.ClinicaVeterinario;
import br.com.fiap.java_afetto.model.clinica.Expediente;
import br.com.fiap.java_afetto.model.endereco.Endereco;
import br.com.fiap.java_afetto.util.FormatoData;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClinicaMapper {

    /**
     * O que não está na tabela da clínica e chega calculado pelo ClinicaService:
     * distância e "perto" (dependem de onde o usuário está), "favorita" (de quem
     * está logado) e o resumo das avaliações (da tabela de avaliações).
     */
    public record DadosCalculados(
            Double distanciaKm,
            boolean perto,
            boolean favorita,
            Double notaMedia,
            long totalAvaliacoes
    ) {
    }

    public ClinicaResponse clinicaToResponse(
            Clinica clinica,
            DadosCalculados dados,
            List<ClinicaVeterinario> equipe,
            List<Expediente> expediente
    ) {

        Link linkClinicas =
                linkTo(
                        methodOn(ClinicaController.class)
                                .readClinicas(0, 50, null, null, null, false)
                )
                        .withRel("Lista de clínicas");

        List<VeterinarioClinica> veterinarios = equipe.stream()
                .map(vinculo -> new VeterinarioClinica(
                        vinculo.getVeterinario().getNome(),
                        vinculo.getVeterinario().getEspecialidade(),
                        vinculo.getTurno()
                ))
                .toList();

        List<ExpedienteClinica> funcionamento = expediente.stream()
                .sorted(Comparator.comparing(Expediente::getDiaSemana))
                .map(dia -> new ExpedienteClinica(
                        FormatoData.diaDaSemana(dia.getDiaSemana()),
                        FormatoData.hora(dia.getHoraAbertura()),
                        FormatoData.hora(dia.getHoraFechamento())
                ))
                .toList();

        return new ClinicaResponse(
                clinica.getId(),
                clinica.getNome(),
                clinica.getDescricao(),
                clinica.getImagemUrl(),
                clinica.getTelefone(),
                clinica.getEmail(),
                clinica.getSite(),
                enderecoDaClinica(clinica),
                dados.distanciaKm(),
                dados.perto(),
                dados.favorita(),
                dados.notaMedia(),
                dados.totalAvaliacoes(),
                veterinarios,
                funcionamento,
                linkClinicas
        );
    }

    public ClinicaLista clinicaToResponseLista(
            Clinica clinica,
            DadosCalculados dados
    ) {

        Link linkClinica =
                linkTo(
                        methodOn(ClinicaController.class)
                                .readClinica(clinica.getId(), null, null)
                )
                        .withRel("Detalhes da clínica");

        return new ClinicaLista(
                clinica.getId(),
                clinica.getNome(),
                clinica.getImagemUrl(),
                enderecoDaClinica(clinica),
                dados.distanciaKm(),
                dados.perto(),
                dados.favorita(),
                dados.notaMedia(),
                dados.totalAvaliacoes(),
                linkClinica
        );
    }

    public HorariosClinicaResponse horariosToResponse(
            Clinica clinica,
            int duracaoMinutos,
            List<DiaDisponivel> dias
    ) {

        Link linkClinica =
                linkTo(
                        methodOn(ClinicaController.class)
                                .readClinica(clinica.getId(), null, null)
                )
                        .withRel("Detalhes da clínica");

        return new HorariosClinicaResponse(
                clinica.getId(),
                clinica.getNome(),
                duracaoMinutos,
                dias,
                linkClinica
        );
    }

    // Achata a hierarquia Logradouro > Bairro > Cidade > Estado num endereço só
    private EnderecoClinica enderecoDaClinica(Clinica clinica) {

        Endereco endereco = clinica.getEndereco();

        return new EnderecoClinica(
                endereco.getLogradouro().getNome(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getLogradouro().getBairro().getNome(),
                endereco.getLogradouro().getBairro().getCidade().getNome(),
                endereco.getLogradouro().getBairro().getCidade().getEstado().getSigla(),
                endereco.getCep(),
                endereco.getLatitude(),
                endereco.getLongitude()
        );
    }
}
