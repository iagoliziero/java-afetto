package br.com.fiap.java_afetto.exception;

/**
 * O pedido é válido, mas esbarra em algo que já existe (ex.: horário que
 * outra pessoa acabou de agendar). O ValidationExceptionHandler transforma
 * em 409 com {"erro": "mensagem"}.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
