package br.com.fiap.java_afetto.exception;

/**
 * Tentativas demais em pouco tempo (ex.: errar os dados do "esqueci a senha"
 * várias vezes). O ValidationExceptionHandler transforma em 429.
 */
public class MuitasTentativasException extends RuntimeException {

    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
