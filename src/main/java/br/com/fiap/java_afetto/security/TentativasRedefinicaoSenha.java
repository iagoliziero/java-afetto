package br.com.fiap.java_afetto.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freio contra chute de dados no "esqueci a senha": depois de 5 tentativas
 * erradas para o mesmo e-mail em 15 minutos, as próximas são recusadas até
 * a janela passar. Fica em memória (zera quando a API reinicia).
 */
@Component
public class TentativasRedefinicaoSenha {

    private static final int MAX_FALHAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private final Clock relogio;
    private final Map<String, Deque<Instant>> falhasPorEmail = new ConcurrentHashMap<>();

    public TentativasRedefinicaoSenha(Clock relogio) {
        this.relogio = relogio;
    }

    public boolean bloqueado(String email) {

        Deque<Instant> falhas = falhasPorEmail.get(email);

        if (falhas == null) {
            return false;
        }

        synchronized (falhas) {
            descartarAntigas(falhas);
            return falhas.size() >= MAX_FALHAS;
        }
    }

    public void registrarFalha(String email) {

        Deque<Instant> falhas = falhasPorEmail.computeIfAbsent(email, chave -> new ArrayDeque<>());

        synchronized (falhas) {
            descartarAntigas(falhas);
            falhas.addLast(relogio.instant());
        }
    }

    public void limpar(String email) {
        falhasPorEmail.remove(email);
    }

    private void descartarAntigas(Deque<Instant> falhas) {

        Instant limite = relogio.instant().minus(JANELA);

        while (!falhas.isEmpty() && falhas.peekFirst().isBefore(limite)) {
            falhas.removeFirst();
        }
    }
}
