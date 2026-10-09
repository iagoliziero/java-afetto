package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 3 — consultas")
class ConsultaApiTests extends ApiTesteBase {

    private static final String DAQUI_A_10_DIAS = LocalDate.now().plusDays(10).toString();
    private static final String HA_10_DIAS = LocalDate.now().minusDays(10).toString();

    private static String consultaJson(String idPet, String data, String hora, String descricao) {
        return """
                {
                  "descricao": "%s",
                  "data": "%s",
                  "hora": "%s",
                  "nomeVeterinario": "Dra. Ana",
                  "observacoes": "Levar a carteirinha",
                  "idPet": "%s"
                }
                """.formatted(descricao, data, hora, idPet);
    }

    private String novaConsulta(UsuarioTeste usuario, String idPet, String data) {

        ClienteApi.Resposta consulta = usuario.cliente()
                .post("/consulta", consultaJson(idPet, data, "14:30", "Check-up"));
        assertEquals(201, consulta.status(), consulta.corpo());

        return consulta.id();
    }

    @Test
    @DisplayName("Consulta futura nasce AGENDADA; lista, remarca, cancela e exclui")
    void cicloDeUmaConsultaAgendada() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");
        ClienteApi cliente = usuario.cliente();

        ClienteApi.Resposta criada = cliente.post("/consulta",
                consultaJson(idPet, DAQUI_A_10_DIAS, "14:30", "Check-up anual"));
        assertEquals(201, criada.status(), criada.corpo());
        assertTrue(criada.corpo().contains("\"AGENDADO\""), criada.corpo());
        assertTrue(criada.corpo().contains("\"CONSULTA\""), criada.corpo());
        assertTrue(criada.corpo().contains("14:30"), criada.corpo());
        String idConsulta = criada.id();

        ClienteApi.Resposta detalhe = cliente.get("/consulta/" + idConsulta);
        assertEquals(200, detalhe.status(), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("Dra. Ana"), detalhe.corpo());

        ClienteApi.Resposta doPet = cliente.get("/consulta?idPet=" + idPet);
        assertEquals(200, doPet.status(), doPet.corpo());
        assertTrue(doPet.corpo().contains(idConsulta), doPet.corpo());

        ClienteApi.Resposta todas = cliente.get("/consulta");
        assertEquals(200, todas.status(), todas.corpo());
        assertTrue(todas.corpo().contains(idConsulta), todas.corpo());

        ClienteApi.Resposta remarcada = cliente.put("/consulta/" + idConsulta,
                consultaJson(idPet, DAQUI_A_10_DIAS, "09:00", "Check-up anual remarcado"));
        assertEquals(200, remarcada.status(), remarcada.corpo());
        assertTrue(remarcada.corpo().contains("09:00"), remarcada.corpo());
        assertTrue(remarcada.corpo().contains("\"AGENDADO\""), remarcada.corpo());

        ClienteApi.Resposta cancelada = cliente.patch("/consulta/" + idConsulta + "/cancelar");
        assertEquals(200, cancelada.status(), cancelada.corpo());
        assertTrue(cancelada.corpo().contains("\"CANCELADO\""), cancelada.corpo());

        ClienteApi.Resposta editarCancelada = cliente.put("/consulta/" + idConsulta,
                consultaJson(idPet, DAQUI_A_10_DIAS, "10:00", "Tentativa"));
        assertEquals(400, editarCancelada.status(), editarCancelada.corpo());
        assertTrue(editarCancelada.corpo().contains("Consulta cancelada não pode ser alterada"), editarCancelada.corpo());

        ClienteApi.Resposta cancelarDeNovo = cliente.patch("/consulta/" + idConsulta + "/cancelar");
        assertEquals(400, cancelarDeNovo.status(), cancelarDeNovo.corpo());
        assertTrue(cancelarDeNovo.corpo().contains("A consulta já está cancelada"), cancelarDeNovo.corpo());

        ClienteApi.Resposta excluida = cliente.delete("/consulta/" + idConsulta);
        assertEquals(204, excluida.status(), excluida.corpo());

        ClienteApi.Resposta depois = cliente.get("/consulta/" + idConsulta);
        assertEquals(404, depois.status(), depois.corpo());
    }

    @Test
    @DisplayName("Consulta passada nasce REALIZADA (CONCLUIDO) e não pode ser cancelada")
    void consultaPassada() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");

        ClienteApi.Resposta criada = usuario.cliente().post("/consulta",
                consultaJson(idPet, HA_10_DIAS, "14:30", "Vacinação"));
        assertEquals(201, criada.status(), criada.corpo());
        assertTrue(criada.corpo().contains("\"CONCLUIDO\""), criada.corpo());

        ClienteApi.Resposta cancelar = usuario.cliente().patch("/consulta/" + criada.id() + "/cancelar");
        assertEquals(400, cancelar.status(), cancelar.corpo());
        assertTrue(cancelar.corpo().contains("Só é possível cancelar uma consulta agendada"), cancelar.corpo());
    }

    @Test
    @DisplayName("Aceita outro tipo de evento e consulta sem horário")
    void tipoEventoEHoraOpcional() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");

        ClienteApi.Resposta retorno = usuario.cliente().post("/consulta", """
                {
                  "tipoEvento": "RETORNO",
                  "descricao": "Retorno da cirurgia",
                  "data": "%s",
                  "idPet": "%s"
                }
                """.formatted(DAQUI_A_10_DIAS, idPet));

        assertEquals(201, retorno.status(), retorno.corpo());
        assertTrue(retorno.corpo().contains("\"RETORNO\""), retorno.corpo());
        assertTrue(retorno.corpo().contains("\"AGENDADO\""), retorno.corpo());
    }

    @Test
    @DisplayName("Sem data ou sem motivo responde 400 apontando o campo")
    void camposObrigatorios() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");

        ClienteApi.Resposta semData = usuario.cliente().post("/consulta", """
                { "descricao": "Check-up", "idPet": "%s" }
                """.formatted(idPet));
        assertEquals(400, semData.status(), semData.corpo());
        assertTrue(semData.corpo().contains("\"data\""), semData.corpo());

        ClienteApi.Resposta semMotivo = usuario.cliente().post("/consulta", """
                { "data": "%s", "idPet": "%s" }
                """.formatted(DAQUI_A_10_DIAS, idPet));
        assertEquals(400, semMotivo.status(), semMotivo.corpo());
        assertTrue(semMotivo.corpo().contains("descricao"), semMotivo.corpo());
    }

    @Test
    @DisplayName("Outro usuário não vê, não altera, não cancela e não exclui a consulta")
    void outroUsuarioNaoAcessa() {

        UsuarioTeste dono = novoUsuarioLogado();
        String idPet = novoPet(dono, "Thor");
        String idConsulta = novaConsulta(dono, idPet, DAQUI_A_10_DIAS);

        ClienteApi outro = novoUsuarioLogado().cliente();

        ClienteApi.Resposta ver = outro.get("/consulta/" + idConsulta);
        assertEquals(404, ver.status(), ver.corpo());

        ClienteApi.Resposta editar = outro.put("/consulta/" + idConsulta,
                consultaJson(idPet, DAQUI_A_10_DIAS, "10:00", "Invasão"));
        assertEquals(404, editar.status(), editar.corpo());

        ClienteApi.Resposta cancelar = outro.patch("/consulta/" + idConsulta + "/cancelar");
        assertEquals(404, cancelar.status(), cancelar.corpo());

        ClienteApi.Resposta excluir = outro.delete("/consulta/" + idConsulta);
        assertEquals(404, excluir.status(), excluir.corpo());

        ClienteApi.Resposta cadastrarNoPetAlheio = outro.post("/consulta",
                consultaJson(idPet, DAQUI_A_10_DIAS, "10:00", "Invasão"));
        assertEquals(404, cadastrarNoPetAlheio.status(), cadastrarNoPetAlheio.corpo());

        ClienteApi.Resposta listaDoOutro = outro.get("/consulta");
        assertEquals(200, listaDoOutro.status(), listaDoOutro.corpo());
        assertFalse(listaDoOutro.corpo().contains(idConsulta), listaDoOutro.corpo());

        // A consulta do dono continua agendada e intacta
        ClienteApi.Resposta doDono = dono.cliente().get("/consulta/" + idConsulta);
        assertEquals(200, doDono.status(), doDono.corpo());
        assertTrue(doDono.corpo().contains("\"AGENDADO\""), doDono.corpo());
        assertTrue(doDono.corpo().contains("Check-up"), doDono.corpo());
    }

    @Test
    @DisplayName("Excluir a consulta mantém a vacina aplicada nela")
    void excluirConsultaMantemVacina() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");
        String idConsulta = novaConsulta(usuario, idPet, HA_10_DIAS);
        String idVacina = vacinaNaConsulta(usuario, idPet, idConsulta);

        ClienteApi.Resposta exclusao = usuario.cliente().delete("/consulta/" + idConsulta);
        assertEquals(204, exclusao.status(), exclusao.corpo());

        ClienteApi.Resposta vacina = usuario.cliente().get("/vacina/" + idVacina);
        assertEquals(200, vacina.status(), vacina.corpo());
    }

    @Test
    @DisplayName("Excluir o pet apaga consultas, vacinas e remédios juntos")
    void excluirPetComTudo() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");
        String idConsulta = novaConsulta(usuario, idPet, HA_10_DIAS);
        vacinaNaConsulta(usuario, idPet, idConsulta);

        ClienteApi.Resposta remedio = usuario.cliente().post("/remedio", """
                { "nomeRemedio": "Vermífugo", "dataInicio": "%s", "idPet": "%s" }
                """.formatted(HA_10_DIAS, idPet));
        assertEquals(201, remedio.status(), remedio.corpo());

        ClienteApi.Resposta exclusao = usuario.cliente().delete("/pet/" + idPet);
        assertEquals(204, exclusao.status(), exclusao.corpo());

        ClienteApi.Resposta consulta = usuario.cliente().get("/consulta/" + idConsulta);
        assertEquals(404, consulta.status(), consulta.corpo());
    }

    // Vacina registrada como aplicada na consulta (idHistorico = id da consulta)
    private String vacinaNaConsulta(UsuarioTeste usuario, String idPet, String idConsulta) {

        ClienteApi.Resposta vacina = usuario.cliente().post("/vacina", """
                {
                  "nomeVacina": "V10",
                  "dataAplicacao": "%s",
                  "idPet": "%s",
                  "idHistorico": "%s"
                }
                """.formatted(HA_10_DIAS, idPet, idConsulta));
        assertEquals(201, vacina.status(), vacina.corpo());

        return vacina.id();
    }
}
