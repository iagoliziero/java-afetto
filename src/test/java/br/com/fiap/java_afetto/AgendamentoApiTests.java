package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Cada teste que agenda usa uma clínica diferente, para um não ocupar o horário do outro
@DisplayName("Passo 7 — horários e agendamento nas clínicas")
class AgendamentoApiTests extends ApiTesteBase {

    private static final String PINHEIROS = "c1a00000-0000-4000-8000-000000000001";
    private static final String LUA_CHEIA = "c1a00000-0000-4000-8000-000000000002";
    private static final String PELO_MACIO = "c1a00000-0000-4000-8000-000000000003";
    private static final String FAREJO = "c1a00000-0000-4000-8000-000000000004";
    private static final String TOCA_DO_BICHO = "c1a00000-0000-4000-8000-000000000005";
    private static final String RONROM = "c1a00000-0000-4000-8000-000000000006";
    private static final String JARDIM_DAS_PATAS = "c1a00000-0000-4000-8000-000000000007";
    private static final String LATIDO_24H = "c1a00000-0000-4000-8000-000000000008";

    private static final Pattern HORARIO = Pattern.compile("\"\\d{2}:\\d{2}\"");

    // Sempre pelo menos 2 dias à frente, para o teste não depender da hora em que roda
    private static LocalDate proxima(DayOfWeek dia) {
        return LocalDate.now().plusDays(2).with(TemporalAdjusters.nextOrSame(dia));
    }

    private static String agendamentoJson(String idPet, LocalDate data, String hora) {
        return """
                { "idPet": "%s", "data": "%s", "hora": "%s" }
                """.formatted(idPet, data, hora);
    }

    private ClienteApi.Resposta horarios(ClienteApi cliente, String idClinica, LocalDate dia) {
        return cliente.get("/clinica/" + idClinica + "/horarios?inicio=" + dia + "&dias=1");
    }

    @Test
    @DisplayName("Dia útil: de 30 em 30 minutos, das 8h até o último horário antes das 18h")
    void horariosDeUmDiaUtil() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta resposta = horarios(cliente, PINHEIROS, proxima(DayOfWeek.MONDAY));

        assertEquals(200, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("\"08:00\""), resposta.corpo());
        assertTrue(resposta.corpo().contains("\"17:30\""), resposta.corpo());
        assertFalse(resposta.corpo().contains("\"18:00\""), resposta.corpo());
        assertEquals(20, HORARIO.matcher(resposta.corpo()).results().count(), resposta.corpo());
        assertTrue(resposta.corpo().contains("segunda-feira"), resposta.corpo());
    }

    @Test
    @DisplayName("Domingo: clínica comum fechada; a 24h aberta o dia todo")
    void domingo() {

        ClienteApi cliente = novoUsuarioLogado().cliente();
        LocalDate domingo = proxima(DayOfWeek.SUNDAY);

        ClienteApi.Resposta fechada = horarios(cliente, PINHEIROS, domingo);
        assertEquals(200, fechada.status(), fechada.corpo());
        assertTrue(Pattern.compile("\"aberta\"\\s*:\\s*false").matcher(fechada.corpo()).find(), fechada.corpo());
        assertTrue(Pattern.compile("\"horarios\"\\s*:\\s*\\[\\s*]").matcher(fechada.corpo()).find(), fechada.corpo());

        ClienteApi.Resposta vinteQuatroHoras = horarios(cliente, LATIDO_24H, domingo);
        assertEquals(200, vinteQuatroHoras.status(), vinteQuatroHoras.corpo());
        assertTrue(vinteQuatroHoras.corpo().contains("\"00:00\""), vinteQuatroHoras.corpo());
        assertTrue(vinteQuatroHoras.corpo().contains("\"23:00\""), vinteQuatroHoras.corpo());
    }

    @Test
    @DisplayName("Agendar cria a consulta AGENDADA no histórico do pet e ocupa o horário para todos")
    void agendar() {

        UsuarioTeste tutor = novoUsuarioLogado();
        String idPet = novoPet(tutor, "Thor");
        LocalDate segunda = proxima(DayOfWeek.MONDAY);

        ClienteApi.Resposta agendamento = tutor.cliente().post("/clinica/" + LUA_CHEIA + "/agendamento",
                agendamentoJson(idPet, segunda, "09:00"));
        assertEquals(201, agendamento.status(), agendamento.corpo());
        assertTrue(agendamento.corpo().contains("\"AGENDADO\""), agendamento.corpo());
        assertTrue(agendamento.corpo().contains("Hospital Veterinário Lua Cheia"), agendamento.corpo());
        assertTrue(agendamento.corpo().contains(LUA_CHEIA), agendamento.corpo());

        ClienteApi.Resposta historico = tutor.cliente().get("/consulta?idPet=" + idPet);
        assertEquals(200, historico.status(), historico.corpo());
        assertTrue(historico.corpo().contains(agendamento.id()), historico.corpo());

        ClienteApi.Resposta livres = horarios(tutor.cliente(), LUA_CHEIA, segunda);
        assertFalse(livres.corpo().contains("\"09:00\""), livres.corpo());
        assertTrue(livres.corpo().contains("\"09:30\""), livres.corpo());

        ClienteApi.Resposta livresParaOutro = horarios(novoUsuarioLogado().cliente(), LUA_CHEIA, segunda);
        assertFalse(livresParaOutro.corpo().contains("\"09:00\""), livresParaOutro.corpo());
    }

    @Test
    @DisplayName("Horário já agendado por outra pessoa responde 409")
    void horarioOcupado() {

        UsuarioTeste primeiro = novoUsuarioLogado();
        UsuarioTeste segundo = novoUsuarioLogado();
        LocalDate segunda = proxima(DayOfWeek.MONDAY);

        ClienteApi.Resposta agendado = primeiro.cliente().post("/clinica/" + PELO_MACIO + "/agendamento",
                agendamentoJson(novoPet(primeiro, "Mia"), segunda, "10:00"));
        assertEquals(201, agendado.status(), agendado.corpo());

        ClienteApi.Resposta conflito = segundo.cliente().post("/clinica/" + PELO_MACIO + "/agendamento",
                agendamentoJson(novoPet(segundo, "Luna"), segunda, "10:00"));
        assertEquals(409, conflito.status(), conflito.corpo());
        assertTrue(conflito.corpo().contains("Esse horário já foi agendado"), conflito.corpo());
    }

    @Test
    @DisplayName("Cancelar o agendamento libera o horário")
    void cancelarLiberaHorario() {

        UsuarioTeste primeiro = novoUsuarioLogado();
        UsuarioTeste segundo = novoUsuarioLogado();
        LocalDate segunda = proxima(DayOfWeek.MONDAY);

        String idConsulta = primeiro.cliente().post("/clinica/" + FAREJO + "/agendamento",
                agendamentoJson(novoPet(primeiro, "Bob"), segunda, "11:00")).id();

        ClienteApi.Resposta cancelada = primeiro.cliente().patch("/consulta/" + idConsulta + "/cancelar");
        assertEquals(200, cancelada.status(), cancelada.corpo());

        ClienteApi.Resposta livres = horarios(segundo.cliente(), FAREJO, segunda);
        assertTrue(livres.corpo().contains("\"11:00\""), livres.corpo());

        ClienteApi.Resposta agendaDeNovo = segundo.cliente().post("/clinica/" + FAREJO + "/agendamento",
                agendamentoJson(novoPet(segundo, "Nina"), segunda, "11:00"));
        assertEquals(201, agendaDeNovo.status(), agendaDeNovo.corpo());
    }

    @Test
    @DisplayName("Fora do expediente responde 400 (horário quebrado, depois das 18h, domingo)")
    void foraDoExpediente() {

        UsuarioTeste tutor = novoUsuarioLogado();
        String idPet = novoPet(tutor, "Thor");
        String caminho = "/clinica/" + TOCA_DO_BICHO + "/agendamento";

        ClienteApi.Resposta noite = tutor.cliente().post(caminho, agendamentoJson(idPet, proxima(DayOfWeek.MONDAY), "19:00"));
        assertEquals(400, noite.status(), noite.corpo());
        assertTrue(noite.corpo().contains("A clínica não atende nesse horário"), noite.corpo());

        ClienteApi.Resposta quebrado = tutor.cliente().post(caminho, agendamentoJson(idPet, proxima(DayOfWeek.MONDAY), "08:15"));
        assertEquals(400, quebrado.status(), quebrado.corpo());

        ClienteApi.Resposta sabadoATarde = tutor.cliente().post(caminho, agendamentoJson(idPet, proxima(DayOfWeek.SATURDAY), "13:00"));
        assertEquals(400, sabadoATarde.status(), sabadoATarde.corpo());

        ClienteApi.Resposta domingo = tutor.cliente().post(caminho, agendamentoJson(idPet, proxima(DayOfWeek.SUNDAY), "10:00"));
        assertEquals(400, domingo.status(), domingo.corpo());
        assertTrue(domingo.corpo().contains("A clínica não atende nesse dia"), domingo.corpo());
    }

    @Test
    @DisplayName("Dia que já passou responde 400")
    void diaQueJaPassou() {

        UsuarioTeste tutor = novoUsuarioLogado();

        ClienteApi.Resposta resposta = tutor.cliente().post("/clinica/" + TOCA_DO_BICHO + "/agendamento",
                agendamentoJson(novoPet(tutor, "Thor"), LocalDate.now().minusDays(1), "10:00"));

        assertEquals(400, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("Escolha um horário a partir de agora"), resposta.corpo());
    }

    @Test
    @DisplayName("Não dá para agendar para o pet de outro usuário")
    void petDeOutroUsuario() {

        UsuarioTeste dono = novoUsuarioLogado();
        String idPet = novoPet(dono, "Thor");

        ClienteApi outro = novoUsuarioLogado().cliente();

        ClienteApi.Resposta resposta = outro.post("/clinica/" + RONROM + "/agendamento",
                agendamentoJson(idPet, proxima(DayOfWeek.MONDAY), "10:00"));

        assertEquals(404, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("Pet não encontrado"), resposta.corpo());
    }

    @Test
    @DisplayName("Remarcar um agendamento só aceita horário livre da clínica")
    void remarcar() {

        UsuarioTeste tutor = novoUsuarioLogado();
        String idPet = novoPet(tutor, "Thor");
        LocalDate segunda = proxima(DayOfWeek.MONDAY);

        String idConsulta = tutor.cliente().post("/clinica/" + JARDIM_DAS_PATAS + "/agendamento",
                agendamentoJson(idPet, segunda, "14:00")).id();

        String remarcacao = """
                { "descricao": "Consulta remarcada", "data": "%s", "hora": "%s", "idPet": "%s" }
                """;

        ClienteApi.Resposta foraDoHorario = tutor.cliente().put("/consulta/" + idConsulta,
                remarcacao.formatted(segunda, "19:00", idPet));
        assertEquals(400, foraDoHorario.status(), foraDoHorario.corpo());

        ClienteApi.Resposta remarcada = tutor.cliente().put("/consulta/" + idConsulta,
                remarcacao.formatted(segunda, "15:00", idPet));
        assertEquals(200, remarcada.status(), remarcada.corpo());
        assertTrue(remarcada.corpo().contains("15:00"), remarcada.corpo());

        ClienteApi.Resposta livres = horarios(tutor.cliente(), JARDIM_DAS_PATAS, segunda);
        assertTrue(livres.corpo().contains("\"14:00\""), livres.corpo());
        assertFalse(livres.corpo().contains("\"15:00\""), livres.corpo());
    }

    @Test
    @DisplayName("O detalhe da clínica traz o horário de funcionamento")
    void detalheComHorarioDeFuncionamento() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta comum = cliente.get("/clinica/" + PINHEIROS);
        assertEquals(200, comum.status(), comum.corpo());
        assertTrue(comum.corpo().contains("segunda-feira"), comum.corpo());
        assertTrue(comum.corpo().contains("\"18:00\""), comum.corpo());
        assertTrue(comum.corpo().contains("sábado"), comum.corpo());
        assertTrue(comum.corpo().contains("\"12:00\""), comum.corpo());
        assertFalse(comum.corpo().contains("domingo"), comum.corpo());

        ClienteApi.Resposta vinteQuatroHoras = cliente.get("/clinica/" + LATIDO_24H);
        assertTrue(vinteQuatroHoras.corpo().contains("domingo"), vinteQuatroHoras.corpo());
    }
}
