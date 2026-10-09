package br.com.fiap.java_afetto.exception;

/**
 * Regra de negócio que o pedido não cumpre (ex.: data de fim antes da data de início).
 * O ValidationExceptionHandler transforma em 400 com {"erro": "mensagem"}.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
