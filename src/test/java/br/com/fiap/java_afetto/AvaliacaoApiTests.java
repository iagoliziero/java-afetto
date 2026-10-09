package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Cada teste usa uma clínica diferente, para as médias de um não mexerem no outro
@DisplayName("Passo 6 — avaliações das clínicas")
class AvaliacaoApiTests extends ApiTesteBase {

    private static final String LUA_CHEIA = "c1a00000-0000-4000-8000-000000000002";
    private static final String PELO_MACIO = "c1a00000-0000-4000-8000-000000000003";
    private static final String TOCA_DO_BICHO = "c1a00000-0000-4000-8000-000000000005";
    private static final String RONROM = "c1a00000-0000-4000-8000-000000000006";
    private static final String JARDIM_DAS_PATAS = "c1a00000-0000-4000-8000-000000000007";

    private static String avaliacaoJson(int nota, String comentario) {
        return """
                { "nota": %d, "comentario": "%s" }
                """.formatted(nota, comentario);
    }

    @Test
    @DisplayName("Avaliar devolve a avaliação com o autor resumido e aparece na lista da clínica")
    void avaliar() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta avaliacao = cliente.put("/clinica/" + LUA_CHEIA + "/avaliacao",
                avaliacaoJson(5, "Atendimento excelente, equipe muito atenciosa"));
        assertEquals(200, avaliacao.status(), avaliacao.corpo());
        assertTrue(avaliacao.corpo().contains("Tutor T."), avaliacao.corpo());
        assertTrue(avaliacao.corpo().contains("Atendimento excelente"), avaliacao.corpo());

        ClienteApi.Resposta lista = cliente.get("/clinica/" + LUA_CHEIA + "/avaliacao");
        assertEquals(200, lista.status(), lista.corpo());
        assertTrue(lista.corpo().contains("Atendimento excelente"), lista.corpo());
        assertEquals("true", campoDoItem(lista.corpo(), avaliacao.id(), "minha"));
    }

    @Test
    @DisplayName("A clínica mostra a média e o total; avaliar de novo atualiza em vez de duplicar")
    void mediaEAtualizacao() {

        ClienteApi primeiro = novoUsuarioLogado().cliente();
        ClienteApi segundo = novoUsuarioLogado().cliente();

        primeiro.put("/clinica/" + PELO_MACIO + "/avaliacao", avaliacaoJson(5, "Ótima com gatos"));
        segundo.put("/clinica/" + PELO_MACIO + "/avaliacao", avaliacaoJson(4, "Gostei"));

        ClienteApi.Resposta detalhe = primeiro.get("/clinica/" + PELO_MACIO);
        assertEquals(200, detalhe.status(), detalhe.corpo());
        assertEquals("4.5", campoDoItem(detalhe.corpo(), PELO_MACIO, "notaMedia"));
        assertEquals("2", campoDoItem(detalhe.corpo(), PELO_MACIO, "totalAvaliacoes"));

        // O primeiro muda de ideia: continua sendo uma avaliação só dele
        ClienteApi.Resposta reavaliacao = primeiro.put("/clinica/" + PELO_MACIO + "/avaliacao", avaliacaoJson(3, "Demorou um pouco"));
        assertEquals(200, reavaliacao.status(), reavaliacao.corpo());

        ClienteApi.Resposta lista = primeiro.get("/clinica");
        assertEquals("3.5", campoDoItem(lista.corpo(), PELO_MACIO, "notaMedia"));
        assertEquals("2", campoDoItem(lista.corpo(), PELO_MACIO, "totalAvaliacoes"));

        ClienteApi.Resposta avaliacoes = primeiro.get("/clinica/" + PELO_MACIO + "/avaliacao");
        assertTrue(avaliacoes.corpo().contains("Demorou um pouco"), avaliacoes.corpo());
        assertFalse(avaliacoes.corpo().contains("Ótima com gatos"), avaliacoes.corpo());
    }

    @Test
    @DisplayName("Clínica sem avaliações vem com média nula e total zero")
    void semAvaliacoes() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta detalhe = cliente.get("/clinica/" + TOCA_DO_BICHO);

        assertEquals(200, detalhe.status(), detalhe.corpo());
        assertEquals("null", campoDoItem(detalhe.corpo(), TOCA_DO_BICHO, "notaMedia"));
        assertEquals("0", campoDoItem(detalhe.corpo(), TOCA_DO_BICHO, "totalAvaliacoes"));
    }

    @Test
    @DisplayName("Nota fora de 1 a 5, ou sem nota, responde 400")
    void notaInvalida() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta notaSeis = cliente.put("/clinica/" + TOCA_DO_BICHO + "/avaliacao", avaliacaoJson(6, "Nota demais"));
        assertEquals(400, notaSeis.status(), notaSeis.corpo());
        assertTrue(notaSeis.corpo().contains("A nota deve ser de 1 a 5"), notaSeis.corpo());

        ClienteApi.Resposta semNota = cliente.put("/clinica/" + TOCA_DO_BICHO + "/avaliacao", """
                { "comentario": "Esqueci a nota" }
                """);
        assertEquals(400, semNota.status(), semNota.corpo());
        assertTrue(semNota.corpo().contains("A nota é obrigatória"), semNota.corpo());
    }

    @Test
    @DisplayName("Apagar a própria avaliação (repetir não dá erro)")
    void apagar() {

        ClienteApi cliente = novoUsuarioLogado().cliente();
        cliente.put("/clinica/" + RONROM + "/avaliacao", avaliacaoJson(2, "Não gostei"));

        ClienteApi.Resposta exclusao = cliente.delete("/clinica/" + RONROM + "/avaliacao");
        assertEquals(204, exclusao.status(), exclusao.corpo());

        ClienteApi.Resposta lista = cliente.get("/clinica/" + RONROM + "/avaliacao");
        assertFalse(lista.corpo().contains("Não gostei"), lista.corpo());

        ClienteApi.Resposta deNovo = cliente.delete("/clinica/" + RONROM + "/avaliacao");
        assertEquals(204, deNovo.status(), deNovo.corpo());
    }

    @Test
    @DisplayName("Outro usuário vê a avaliação, mas ela não é \"minha\" e ele não consegue apagá-la")
    void outroUsuario() {

        ClienteApi dono = novoUsuarioLogado().cliente();
        String idAvaliacao = dono.put("/clinica/" + JARDIM_DAS_PATAS + "/avaliacao", avaliacaoJson(4, "Bom atendimento")).id();

        ClienteApi outro = novoUsuarioLogado().cliente();

        ClienteApi.Resposta listaDoOutro = outro.get("/clinica/" + JARDIM_DAS_PATAS + "/avaliacao");
        assertEquals(200, listaDoOutro.status(), listaDoOutro.corpo());
        assertEquals("false", campoDoItem(listaDoOutro.corpo(), idAvaliacao, "minha"));

        // O DELETE só apaga a avaliação de quem chama: a do dono continua lá
        assertEquals(204, outro.delete("/clinica/" + JARDIM_DAS_PATAS + "/avaliacao").status());

        ClienteApi.Resposta listaDoDono = dono.get("/clinica/" + JARDIM_DAS_PATAS + "/avaliacao");
        assertTrue(listaDoDono.corpo().contains("Bom atendimento"), listaDoDono.corpo());
    }

    @Test
    @DisplayName("Avaliar clínica que não existe responde 404")
    void clinicaInexistente() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta resposta = cliente.put("/clinica/" + UUID.randomUUID() + "/avaliacao", avaliacaoJson(5, "Ótima"));

        assertEquals(404, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("Clínica não encontrada"), resposta.corpo());
    }
}
