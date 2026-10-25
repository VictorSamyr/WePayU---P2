package br.ufal.ic.p2.wepayu.exceptions;

public class ValorPositivoException extends Exception {
    public ValorPositivoException() {
        super("Valor deve ser positivo.");
    }

    public ValorPositivoException(String mensagem) {
        super(mensagem);
    }
}
