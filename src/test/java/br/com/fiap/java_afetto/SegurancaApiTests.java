package br.com.fiap.java_afetto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cada conta só vê e mexe nos próprios dados. Pet, vacina ou usuário de outra
 * conta responde 404 — igual a um id que não existe, para não revelar nada.
 */
@DisplayName("Segurança — login 401 e cada conta só acessa os próprios dados")
class SegurancaApiTests extends ApiTesteBase {

    // O idUsuario é obrigatório no corpo do pet; aqui vai o de quem faz o pedido
    private static String petEditado(UsuarioTeste quemEdita) {
        return """
                {
                  "nome": "Invadido",
                  "especie": "GATO",
                  "raca": "SRD",
                  "sexo": "FEMEA",
                  "peso": 3.0,
                  "dataNasc": "2021-01-01",
                  "idUsuario": "%s"
                }
                """.formatted(quemEdita.id());
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Senha errada no login: 401 com mensagem")
    void loginComSenhaErrada() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta login = novoCliente().post("/login", """
                { "email": "%s", "senha": "senhaErrada" }
                """.formatted(ana.email()));

        assertEquals(401, login.status(), login.corpo());
        assertTrue(login.corpo().contains("E-mail ou senha incorretos"), login.corpo());
    }

    @Test
    @DisplayName("E-mail que não tem conta: o mesmo 401 (não revela quem tem conta)")
    void loginComEmailInexistente() {

        ClienteApi.Resposta login = novoCliente().post("/login", """
                { "email": "ninguem-%s@afetto.com", "senha": "senha123" }
                """.formatted(UUID.randomUUID()));

        assertEquals(401, login.status(), login.corpo());
        assertTrue(login.corpo().contains("E-mail ou senha incorretos"), login.corpo());
    }

    @Test
    @DisplayName("Sem login (ou com a sessão expirada): 401")
    void semLoginResponde401() {

        assertEquals(401, novoCliente().get("/pet").status());
        assertEquals(401, novoCliente().get("/vacina").status());
        assertEquals(401, novoCliente().get("/usuario/me").status());
    }

    // ─── PETS ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Pet de outra conta: GET, PUT, DELETE e status de vacinação respondem 404, e o pet continua intacto")
    void petDeOutraConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();
        String petDaAna = novoPet(ana, "Thor");

        assertEquals(404, bruno.cliente().get("/pet/" + petDaAna).status());
        assertEquals(404, bruno.cliente().put("/pet/" + petDaAna, petEditado(bruno)).status());
        assertEquals(404, bruno.cliente().get("/pet/" + petDaAna + "/status-vacinacao").status());
        assertEquals(404, bruno.cliente().delete("/pet/" + petDaAna).status());

        ClienteApi.Resposta daAna = ana.cliente().get("/pet/" + petDaAna);
        assertEquals(200, daAna.status(), daAna.corpo());
        assertTrue(daAna.corpo().contains("Thor"), daAna.corpo());
        assertFalse(daAna.corpo().contains("Invadido"), daAna.corpo());
    }

    @Test
    @DisplayName("O dono continua lendo, editando e excluindo o próprio pet")
    void donoMexeNoProprioPet() {

        UsuarioTeste ana = novoUsuarioLogado();
        String pet = novoPet(ana, "Thor");

        assertEquals(200, ana.cliente().get("/pet/" + pet + "/status-vacinacao").status());

        ClienteApi.Resposta editado = ana.cliente().put("/pet/" + pet, petEditado(ana));
        assertEquals(200, editado.status(), editado.corpo());
        assertTrue(editado.corpo().contains("Invadido"), editado.corpo());

        assertEquals(204, ana.cliente().delete("/pet/" + pet).status());
        assertEquals(404, ana.cliente().get("/pet/" + pet).status());
    }

    @Test
    @DisplayName("Criar pet na conta de outra pessoa: 404, e nada aparece na conta dela")
    void criarPetSoNaPropriaConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();

        ClienteApi.Resposta naContaDaAna = bruno.cliente().post("/pet", """
                {
                  "nome": "Intruso",
                  "especie": "CACHORRO",
                  "sexo": "MACHO",
                  "dataNasc": "2022-03-10",
                  "idUsuario": "%s"
                }
                """.formatted(ana.id()));
        assertEquals(404, naContaDaAna.status(), naContaDaAna.corpo());
        assertFalse(ana.cliente().get("/pet").corpo().contains("Intruso"));
        assertFalse(bruno.cliente().get("/pet").corpo().contains("Intruso"));
    }

    @Test
    @DisplayName("Editar o próprio pet não passa o pet para outra conta")
    void naoPassaPetParaOutraConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();
        String pet = novoPet(ana, "Thor");

        ClienteApi.Resposta passar = ana.cliente().put("/pet/" + pet, """
                {
                  "nome": "Thor",
                  "especie": "CACHORRO",
                  "sexo": "MACHO",
                  "dataNasc": "2022-03-10",
                  "idUsuario": "%s"
                }
                """.formatted(bruno.id()));

        assertEquals(404, passar.status(), passar.corpo());
        assertEquals(200, ana.cliente().get("/pet/" + pet).status());
        assertEquals(404, bruno.cliente().get("/pet/" + pet).status());
    }

    // ─── VACINAS ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /vacina sem ?idPet= traz só as vacinas dos pets da própria conta")
    void listaDeVacinasSoDaConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();
        String vacinaDaAna = "V10-" + UUID.randomUUID();
        String vacinaDoBruno = "Antirrabica-" + UUID.randomUUID();
        novaVacina(ana, novoPet(ana, "Thor"), vacinaDaAna);
        novaVacina(bruno, novoPet(bruno, "Rex"), vacinaDoBruno);

        ClienteApi.Resposta lista = bruno.cliente().get("/vacina");

        assertEquals(200, lista.status(), lista.corpo());
        assertTrue(lista.corpo().contains(vacinaDoBruno), lista.corpo());
        assertFalse(lista.corpo().contains(vacinaDaAna), lista.corpo());
    }

    @Test
    @DisplayName("Vacina e pet de outra conta: listar, ler, criar, editar e excluir respondem 404")
    void vacinaDeOutraConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();
        String petDaAna = novoPet(ana, "Thor");
        String vacinaDaAna = novaVacina(ana, petDaAna, "V10");
        String vacinaEditada = """
                {
                  "nomeVacina": "Trocada",
                  "dataAplicacao": "2026-09-01",
                  "idPet": "%s"
                }
                """.formatted(petDaAna);

        assertEquals(404, bruno.cliente().get("/vacina?idPet=" + petDaAna).status());
        assertEquals(404, bruno.cliente().get("/vacina/" + vacinaDaAna).status());
        assertEquals(404, bruno.cliente().post("/vacina", vacinaEditada).status());
        assertEquals(404, bruno.cliente().put("/vacina/" + vacinaDaAna, vacinaEditada).status());
        assertEquals(404, bruno.cliente().delete("/vacina/" + vacinaDaAna).status());

        ClienteApi.Resposta daAna = ana.cliente().get("/vacina/" + vacinaDaAna);
        assertEquals(200, daAna.status(), daAna.corpo());
        assertTrue(daAna.corpo().contains("V10"), daAna.corpo());
    }

    @Test
    @DisplayName("Não dá para mover a própria vacina para o pet de outra conta")
    void naoMoveVacinaParaPetDeOutraConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();
        String vacinaDoBruno = novaVacina(bruno, novoPet(bruno, "Rex"), "V10");
        String petDaAna = novoPet(ana, "Thor");

        ClienteApi.Resposta mover = bruno.cliente().put("/vacina/" + vacinaDoBruno, """
                {
                  "nomeVacina": "V10",
                  "dataAplicacao": "2026-09-01",
                  "idPet": "%s"
                }
                """.formatted(petDaAna));

        assertEquals(404, mover.status(), mover.corpo());
        assertEquals(200, bruno.cliente().get("/vacina/" + vacinaDoBruno).status());
    }

    // ─── USUÁRIO ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Outra conta: GET e PUT /usuario/{id} respondem 404, e os dados dela não mudam")
    void usuarioDeOutraConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();

        assertEquals(404, bruno.cliente().get("/usuario/" + ana.id()).status());

        ClienteApi.Resposta editar = bruno.cliente().put("/usuario/" + ana.id(), """
                {
                  "nome": "Invadida",
                  "cpf": "12345678909",
                  "dataNascimento": "1995-05-20",
                  "email": "%s",
                  "telefone": "11900000000"
                }
                """.formatted(ana.email()));
        assertEquals(404, editar.status(), editar.corpo());

        ClienteApi.Resposta daAna = ana.cliente().get("/usuario/" + ana.id());
        assertEquals(200, daAna.status(), daAna.corpo());
        assertFalse(daAna.corpo().contains("Invadida"), daAna.corpo());
    }

    @Test
    @DisplayName("USER não lista todas as contas nem troca role (nem a própria): 403")
    void soAdminListaContasETrocaRole() {

        UsuarioTeste ana = novoUsuarioLogado();

        assertEquals(403, ana.cliente().get("/usuario").status());

        ClienteApi.Resposta virarAdmin = ana.cliente().patch("/usuario/" + ana.id() + "/role", """
                { "role": "ADMIN" }
                """);
        assertEquals(403, virarAdmin.status(), virarAdmin.corpo());

        // Entrando de novo, continua USER: a lista de contas segue proibida
        ClienteApi deNovo = novoCliente();
        assertEquals(200, deNovo.post("/login", """
                { "email": "%s", "senha": "senha123" }
                """.formatted(ana.email())).status());
        assertEquals(403, deNovo.get("/usuario").status());
    }

    @Test
    @DisplayName("Cadastro com e-mail que já tem conta: 409 com mensagem (maiúsculas não enganam)")
    void cadastroComEmailRepetido() {

        UsuarioTeste ana = novoUsuarioLogado();

        ClienteApi.Resposta repetido = novoCliente().post("/usuario", """
                {
                  "nome": "Outra Ana",
                  "cpf": "12345678909",
                  "dataNascimento": "1995-05-20",
                  "email": "%s",
                  "senha": "senha123",
                  "telefone": "11999999999"
                }
                """.formatted(ana.email().toUpperCase()));

        assertEquals(409, repetido.status(), repetido.corpo());
        assertTrue(repetido.corpo().contains("Este e-mail já está cadastrado"), repetido.corpo());
    }

    @Test
    @DisplayName("Trocar o próprio e-mail para o de outra conta: 409")
    void editarComEmailDeOutraConta() {

        UsuarioTeste ana = novoUsuarioLogado();
        UsuarioTeste bruno = novoUsuarioLogado();

        ClienteApi.Resposta editar = bruno.cliente().put("/usuario/" + bruno.id(), """
                {
                  "nome": "Bruno",
                  "cpf": "12345678909",
                  "dataNascimento": "1995-05-20",
                  "email": "%s",
                  "telefone": "11900000000"
                }
                """.formatted(ana.email()));

        assertEquals(409, editar.status(), editar.corpo());
        assertTrue(editar.corpo().contains("Este e-mail já está cadastrado"), editar.corpo());
    }

    @Test
    @DisplayName("Trocar o próprio e-mail mantém a sessão, e o login passa a ser com o e-mail novo")
    void trocarOProprioEmailMantemASessao() {

        UsuarioTeste ana = novoUsuarioLogado();
        String emailNovo = "nova-" + UUID.randomUUID() + "@afetto.com";

        ClienteApi.Resposta editar = ana.cliente().put("/usuario/" + ana.id(), """
                {
                  "nome": "Ana",
                  "cpf": "12345678909",
                  "dataNascimento": "1995-05-20",
                  "email": "%s",
                  "telefone": "11900000000"
                }
                """.formatted(emailNovo));
        assertEquals(200, editar.status(), editar.corpo());

        // Mesma sessão, sem novo login: continua funcionando
        ClienteApi.Resposta me = ana.cliente().get("/usuario/me");
        assertEquals(200, me.status(), me.corpo());
        assertTrue(me.corpo().contains(emailNovo), me.corpo());
        assertEquals(200, ana.cliente().get("/pet").status());

        assertEquals(200, novoCliente().post("/login", """
                { "email": "%s", "senha": "senha123" }
                """.formatted(emailNovo)).status());
        assertEquals(401, novoCliente().post("/login", """
                { "email": "%s", "senha": "senha123" }
                """.formatted(ana.email())).status());
    }
}
