package br.ufal.ic.p2.wepayu.exceptions;

public class MetodoPagamentoInvalidoException extends Exception {
    public MetodoPagamentoInvalidoException() {
        super("Metodo de pagamento invalido.");
    }

    public MetodoPagamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
