package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Passo 2 — remédios")
class RemedioApiTests extends ApiTesteBase {

    private static String remedioJson(String idPet, String dosagem, String dataFim) {
        return """
                {
                  "nomeRemedio": "Amoxicilina",
                  "dosagem": "%s",
                  "frequencia": "A cada 12 horas",
                  "dataInicio": "2026-10-08",
                  "dataFim": "%s",
                  "observacoes": "Dar junto com a ração",
                  "idPet": "%s"
                }
                """.formatted(dosagem, dataFim, idPet);
    }

    private String novoRemedio(UsuarioTeste usuario, String idPet) {

        ClienteApi.Resposta remedio = usuario.cliente()
                .post("/remedio", remedioJson(idPet, "1 comprimido", "2026-10-15"));
        assertEquals(201, remedio.status(), remedio.corpo());

        return remedio.id();
    }

    @Test
    @DisplayName("Cadastra, lista, edita e exclui um remédio")
    void crudCompleto() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");
        ClienteApi cliente = usuario.cliente();

        ClienteApi.Resposta criado = cliente.post("/remedio", remedioJson(idPet, "1 comprimido", "2026-10-15"));
        assertEquals(201, criado.status(), criado.corpo());
        assertTrue(criado.corpo().contains("\"idPet\""), criado.corpo());
        String idRemedio = criado.id();

        ClienteApi.Resposta detalhe = cliente.get("/remedio/" + idRemedio);
        assertEquals(200, detalhe.status(), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("Amoxicilina"), detalhe.corpo());
        assertTrue(detalhe.corpo().contains("A cada 12 horas"), detalhe.corpo());

        ClienteApi.Resposta doPet = cliente.get("/remedio?idPet=" + idPet);
        assertEquals(200, doPet.status(), doPet.corpo());
        assertTrue(doPet.corpo().contains(idRemedio), doPet.corpo());

        ClienteApi.Resposta todos = cliente.get("/remedio");
        assertEquals(200, todos.status(), todos.corpo());
        assertTrue(todos.corpo().contains(idRemedio), todos.corpo());

        ClienteApi.Resposta editado = cliente.put("/remedio/" + idRemedio, remedioJson(idPet, "2 comprimidos", "2026-10-20"));
        assertEquals(200, editado.status(), editado.corpo());
        assertTrue(editado.corpo().contains("2 comprimidos"), editado.corpo());
        assertTrue(editado.corpo().contains("2026-10-20"), editado.corpo());

        ClienteApi.Resposta excluido = cliente.delete("/remedio/" + idRemedio);
        assertEquals(204, excluido.status(), excluido.corpo());

        ClienteApi.Resposta depois = cliente.get("/remedio/" + idRemedio);
        assertEquals(404, depois.status(), depois.corpo());
    }

    @Test
    @DisplayName("Data de fim antes da de início responde 400 com a mensagem")
    void dataFimAntesDoInicio() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");

        ClienteApi.Resposta resposta = usuario.cliente()
                .post("/remedio", remedioJson(idPet, "1 comprimido", "2026-10-01"));

        assertEquals(400, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("A data de fim não pode ser antes da data de início"), resposta.corpo());
    }

    @Test
    @DisplayName("Sem o nome do remédio responde 400 apontando o campo")
    void nomeObrigatorio() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");

        ClienteApi.Resposta resposta = usuario.cliente().post("/remedio", """
                { "dataInicio": "2026-10-08", "idPet": "%s" }
                """.formatted(idPet));

        assertEquals(400, resposta.status(), resposta.corpo());
        assertTrue(resposta.corpo().contains("nomeRemedio"), resposta.corpo());
    }

    @Test
    @DisplayName("Outro usuário não vê, não edita e não exclui o remédio")
    void outroUsuarioNaoAcessa() {

        UsuarioTeste dono = novoUsuarioLogado();
        String idPet = novoPet(dono, "Thor");
        String idRemedio = novoRemedio(dono, idPet);

        ClienteApi outro = novoUsuarioLogado().cliente();

        ClienteApi.Resposta ver = outro.get("/remedio/" + idRemedio);
        assertEquals(404, ver.status(), ver.corpo());

        ClienteApi.Resposta editar = outro.put("/remedio/" + idRemedio, remedioJson(idPet, "9 comprimidos", "2026-10-15"));
        assertEquals(404, editar.status(), editar.corpo());

        ClienteApi.Resposta excluir = outro.delete("/remedio/" + idRemedio);
        assertEquals(404, excluir.status(), excluir.corpo());

        ClienteApi.Resposta cadastrarNoPetAlheio = outro.post("/remedio", remedioJson(idPet, "1 comprimido", "2026-10-15"));
        assertEquals(404, cadastrarNoPetAlheio.status(), cadastrarNoPetAlheio.corpo());

        ClienteApi.Resposta listarPetAlheio = outro.get("/remedio?idPet=" + idPet);
        assertEquals(404, listarPetAlheio.status(), listarPetAlheio.corpo());

        ClienteApi.Resposta listaDoOutro = outro.get("/remedio");
        assertEquals(200, listaDoOutro.status(), listaDoOutro.corpo());
        assertFalse(listaDoOutro.corpo().contains(idRemedio), listaDoOutro.corpo());

        // O dono continua vendo o remédio intacto
        ClienteApi.Resposta doDono = dono.cliente().get("/remedio/" + idRemedio);
        assertEquals(200, doDono.status(), doDono.corpo());
        assertTrue(doDono.corpo().contains("1 comprimido"), doDono.corpo());
    }

    @Test
    @DisplayName("Excluir um pet com vacina e remédio apaga tudo junto")
    void excluirPetComCuidados() {

        UsuarioTeste usuario = novoUsuarioLogado();
        String idPet = novoPet(usuario, "Thor");
        novaVacina(usuario, idPet, "V10-" + UUID.randomUUID());
        String idRemedio = novoRemedio(usuario, idPet);

        ClienteApi.Resposta exclusao = usuario.cliente().delete("/pet/" + idPet);
        assertEquals(204, exclusao.status(), exclusao.corpo());

        ClienteApi.Resposta pet = usuario.cliente().get("/pet/" + idPet);
        assertEquals(404, pet.status(), pet.corpo());

        ClienteApi.Resposta remedio = usuario.cliente().get("/remedio/" + idRemedio);
        assertEquals(404, remedio.status(), remedio.corpo());
    }
}
