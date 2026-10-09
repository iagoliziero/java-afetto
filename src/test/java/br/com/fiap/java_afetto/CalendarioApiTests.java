package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 8 — calendário")
class CalendarioApiTests extends ApiTesteBase {

    // Clínica que nenhum outro teste usa para agendar
    private static final String RONROM = "c1a00000-0000-4000-8000-000000000006";

    // Datas bem à frente, para o resultado não depender do dia em que o teste roda
    private final LocalDate base = LocalDate.now().plusDays(40);

    private record Cenario(UsuarioTeste tutor, String idPetA, String idPetB) {
    }

    /**
     * Pet A: vacina aplicada em base, próxima dose em base+10, remédio de base+2 a base+4.
     * Pet B: consulta em base+5 às 14:30 e uma consulta cancelada em base+6.
     */
    private Cenario montarCenario() {

        UsuarioTeste tutor = novoUsuarioLogado();
        String idPetA = novoPet(tutor, "Thor");
        String idPetB = novoPet(tutor, "Mel");
        ClienteApi cliente = tutor.cliente();

        assertEquals(201, cliente.post("/vacina", """
                { "nomeVacina": "V10", "dataAplicacao": "%s", "proximaDose": "%s", "idPet": "%s" }
                """.formatted(base, base.plusDays(10), idPetA)).status());

        assertEquals(201, cliente.post("/remedio", """
                { "nomeRemedio": "Amoxicilina", "dosagem": "1 comprimido", "frequencia": "A cada 12 horas",
                  "dataInicio": "%s", "dataFim": "%s", "idPet": "%s" }
                """.formatted(base.plusDays(2), base.plusDays(4), idPetA)).status());

        assertEquals(201, cliente.post("/consulta", """
                { "descricao": "Check-up da Mel", "data": "%s", "hora": "14:30", "nomeVeterinario": "Dra. Ana", "idPet": "%s" }
                """.formatted(base.plusDays(5), idPetB)).status());

        String idCancelada = cliente.post("/consulta", """
                { "descricao": "Banho cancelado", "data": "%s", "idPet": "%s" }
                """.formatted(base.plusDays(6), idPetB)).id();
        assertEquals(200, cliente.patch("/consulta/" + idCancelada + "/cancelar").status());

        return new Cenario(tutor, idPetA, idPetB);
    }

    private String periodo(LocalDate inicio, LocalDate fim) {
        return "inicio=" + inicio + "&fim=" + fim;
    }

    private static long quantos(String corpo, String tipo) {
        return Pattern.compile("\"tipo\"\\s*:\\s*\"" + tipo + "\"").matcher(corpo).results().count();
    }

    private static int posicao(String corpo, String tipo) {
        Matcher matcher = Pattern.compile("\"tipo\"\\s*:\\s*\"" + tipo + "\"").matcher(corpo);
        assertTrue(matcher.find(), "faltou " + tipo + ": " + corpo);
        return matcher.start();
    }

    @Test
    @DisplayName("Junta vacina, próxima dose, remédio (um por dia) e consulta, em ordem de data, sem as canceladas")
    void juntaTudoEmOrdem() {

        Cenario cenario = montarCenario();

        ClienteApi.Resposta calendario = cenario.tutor().cliente()
                .get("/calendario?" + periodo(base, base.plusDays(20)));

        assertEquals(200, calendario.status(), calendario.corpo());
        String corpo = calendario.corpo();
        assertTrue(Pattern.compile("\"totalEventos\"\\s*:\\s*6").matcher(corpo).find(), corpo);
        assertEquals(1, quantos(corpo, "VACINA"), corpo);
        assertEquals(1, quantos(corpo, "PROXIMA_DOSE"), corpo);
        assertEquals(3, quantos(corpo, "REMEDIO"), corpo);
        assertEquals(1, quantos(corpo, "CONSULTA"), corpo);
        assertFalse(corpo.contains("Banho cancelado"), corpo);

        assertTrue(posicao(corpo, "VACINA") < posicao(corpo, "REMEDIO"), corpo);
        assertTrue(posicao(corpo, "REMEDIO") < posicao(corpo, "CONSULTA"), corpo);
        assertTrue(posicao(corpo, "CONSULTA") < posicao(corpo, "PROXIMA_DOSE"), corpo);

        assertTrue(corpo.contains("1 comprimido · A cada 12 horas"), corpo);
        assertTrue(corpo.contains("\"14:30\""), corpo);
        assertTrue(corpo.contains("\"AGENDADO\""), corpo);
        assertTrue(corpo.contains("Dra. Ana"), corpo);
        assertTrue(corpo.contains("\"Mel\""), corpo);
    }

    @Test
    @DisplayName("Filtra por pet")
    void filtraPorPet() {

        Cenario cenario = montarCenario();

        ClienteApi.Resposta soDoThor = cenario.tutor().cliente()
                .get("/calendario?" + periodo(base, base.plusDays(20)) + "&idPet=" + cenario.idPetA());

        assertEquals(200, soDoThor.status(), soDoThor.corpo());
        assertTrue(Pattern.compile("\"totalEventos\"\\s*:\\s*5").matcher(soDoThor.corpo()).find(), soDoThor.corpo());
        assertFalse(soDoThor.corpo().contains("Check-up da Mel"), soDoThor.corpo());
    }

    @Test
    @DisplayName("Respeita o período pedido")
    void respeitaOPeriodo() {

        Cenario cenario = montarCenario();
        LocalDate umDia = base.plusDays(3);

        ClienteApi.Resposta calendario = cenario.tutor().cliente().get("/calendario?" + periodo(umDia, umDia));

        assertEquals(200, calendario.status(), calendario.corpo());
        assertTrue(Pattern.compile("\"totalEventos\"\\s*:\\s*1").matcher(calendario.corpo()).find(), calendario.corpo());
        assertEquals(1, quantos(calendario.corpo(), "REMEDIO"), calendario.corpo());
    }

    @Test
    @DisplayName("Remédio sem data de fim aparece todos os dias até o fim do período")
    void remedioContinuo() {

        UsuarioTeste tutor = novoUsuarioLogado();
        String idPet = novoPet(tutor, "Thor");
        tutor.cliente().post("/remedio", """
                { "nomeRemedio": "Suplemento", "dataInicio": "%s", "idPet": "%s" }
                """.formatted(base.plusDays(15), idPet));

        ClienteApi.Resposta calendario = tutor.cliente().get("/calendario?" + periodo(base, base.plusDays(20)));

        assertEquals(200, calendario.status(), calendario.corpo());
        assertEquals(6, quantos(calendario.corpo(), "REMEDIO"), calendario.corpo());
    }

    @Test
    @DisplayName("Agendamento numa clínica aparece com o nome da clínica")
    void agendamentoNaClinica() {

        UsuarioTeste tutor = novoUsuarioLogado();
        String idPet = novoPet(tutor, "Thor");
        LocalDate segunda = LocalDate.now().plusDays(2).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        ClienteApi.Resposta agendamento = tutor.cliente().post("/clinica/" + RONROM + "/agendamento", """
                { "idPet": "%s", "data": "%s", "hora": "16:00" }
                """.formatted(idPet, segunda));
        assertEquals(201, agendamento.status(), agendamento.corpo());

        ClienteApi.Resposta calendario = tutor.cliente().get("/calendario?" + periodo(segunda, segunda));

        assertEquals(200, calendario.status(), calendario.corpo());
        assertTrue(calendario.corpo().contains("Clínica Veterinária Ronrom"), calendario.corpo());
        assertTrue(calendario.corpo().contains("\"16:00\""), calendario.corpo());
    }

    @Test
    @DisplayName("Outro usuário não vê os eventos, e filtrar pelo pet alheio dá 404")
    void outroUsuario() {

        Cenario cenario = montarCenario();
        ClienteApi outro = novoUsuarioLogado().cliente();

        ClienteApi.Resposta calendarioDoOutro = outro.get("/calendario?" + periodo(base, base.plusDays(20)));
        assertEquals(200, calendarioDoOutro.status(), calendarioDoOutro.corpo());
        assertTrue(Pattern.compile("\"totalEventos\"\\s*:\\s*0").matcher(calendarioDoOutro.corpo()).find(), calendarioDoOutro.corpo());

        ClienteApi.Resposta petAlheio = outro.get("/calendario?idPet=" + cenario.idPetA());
        assertEquals(404, petAlheio.status(), petAlheio.corpo());
    }

    @Test
    @DisplayName("Período inválido responde 400; sem período, usa o mês atual")
    void periodos() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta invertido = cliente.get("/calendario?" + periodo(base, base.minusDays(1)));
        assertEquals(400, invertido.status(), invertido.corpo());
        assertTrue(invertido.corpo().contains("A data final não pode ser antes da inicial"), invertido.corpo());

        ClienteApi.Resposta longoDemais = cliente.get("/calendario?" + periodo(base, base.plusYears(2)));
        assertEquals(400, longoDemais.status(), longoDemais.corpo());
        assertTrue(longoDemais.corpo().contains("O período pode ter no máximo 1 ano"), longoDemais.corpo());

        ClienteApi.Resposta mesAtual = cliente.get("/calendario");
        assertEquals(200, mesAtual.status(), mesAtual.corpo());
        assertTrue(mesAtual.corpo().contains("\"inicio\""), mesAtual.corpo());
    }
}
