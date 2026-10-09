package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 5 — clínicas favoritas")
class ClinicaFavoritaApiTests extends ApiTesteBase {

    // Clínicas fictícias carregadas pela migration V5
    private static final String PINHEIROS = "c1a00000-0000-4000-8000-000000000001";
    private static final String MOOCA = "c1a00000-0000-4000-8000-000000000008";

    @Test
    @DisplayName("Favoritar marca a clínica na lista, no detalhe e no filtro de favoritas")
    void favoritar() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta favoritar = cliente.put("/clinica/" + PINHEIROS + "/favorito", null);
        assertEquals(204, favoritar.status(), favoritar.corpo());

        ClienteApi.Resposta detalhe = cliente.get("/clinica/" + PINHEIROS);
        assertEquals(200, detalhe.status(), detalhe.corpo());
        assertTrue(Pattern.compile("\"favorita\"\\s*:\\s*true").matcher(detalhe.corpo()).find(), detalhe.corpo());

        ClienteApi.Resposta lista = cliente.get("/clinica");
        assertEquals(200, lista.status(), lista.corpo());
        assertEquals("true", campoDoItem(lista.corpo(), PINHEIROS, "favorita"));
        assertEquals("false", campoDoItem(lista.corpo(), MOOCA, "favorita"));

        ClienteApi.Resposta favoritas = cliente.get("/clinica?favoritas=true");
        assertEquals(200, favoritas.status(), favoritas.corpo());
        assertTrue(favoritas.corpo().contains(PINHEIROS), favoritas.corpo());
        assertFalse(favoritas.corpo().contains(MOOCA), favoritas.corpo());
    }

    @Test
    @DisplayName("Favoritar duas vezes não duplica")
    void favoritarDuasVezes() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        assertEquals(204, cliente.put("/clinica/" + PINHEIROS + "/favorito", null).status());
        assertEquals(204, cliente.put("/clinica/" + PINHEIROS + "/favorito", null).status());

        ClienteApi.Resposta favoritas = cliente.get("/clinica?favoritas=true");
        assertEquals(200, favoritas.status(), favoritas.corpo());
        long quantas = Pattern.compile("\"id\"\\s*:\\s*\"" + PINHEIROS + "\"").matcher(favoritas.corpo()).results().count();
        assertEquals(1L, quantas, favoritas.corpo());
    }

    @Test
    @DisplayName("Desfavoritar tira a clínica das favoritas (e repetir não dá erro)")
    void desfavoritar() {

        ClienteApi cliente = novoUsuarioLogado().cliente();
        cliente.put("/clinica/" + PINHEIROS + "/favorito", null);

        ClienteApi.Resposta desfavoritar = cliente.delete("/clinica/" + PINHEIROS + "/favorito");
        assertEquals(204, desfavoritar.status(), desfavoritar.corpo());

        ClienteApi.Resposta favoritas = cliente.get("/clinica?favoritas=true");
        assertEquals(200, favoritas.status(), favoritas.corpo());
        assertFalse(favoritas.corpo().contains(PINHEIROS), favoritas.corpo());

        ClienteApi.Resposta deNovo = cliente.delete("/clinica/" + PINHEIROS + "/favorito");
        assertEquals(204, deNovo.status(), deNovo.corpo());
    }

    @Test
    @DisplayName("As favoritas são de cada usuário")
    void favoritasSaoPorUsuario() {

        ClienteApi dono = novoUsuarioLogado().cliente();
        dono.put("/clinica/" + PINHEIROS + "/favorito", null);

        ClienteApi outro = novoUsuarioLogado().cliente();

        ClienteApi.Resposta favoritasDoOutro = outro.get("/clinica?favoritas=true");
        assertEquals(200, favoritasDoOutro.status(), favoritasDoOutro.corpo());
        assertFalse(favoritasDoOutro.corpo().contains(PINHEIROS), favoritasDoOutro.corpo());

        ClienteApi.Resposta listaDoOutro = outro.get("/clinica");
        assertEquals("false", campoDoItem(listaDoOutro.corpo(), PINHEIROS, "favorita"));
    }

    @Test
    @DisplayName("Favoritar clínica que não existe responde 404")
    void clinicaInexistente() {

        ClienteApi cliente = novoUsuarioLogado().cliente();

        ClienteApi.Resposta resposta = cliente.put("/clinica/" + UUID.randomUUID() + "/favorito", null);

        assertEquals(404, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("Clínica não encontrada"), resposta.corpo());
    }
}
