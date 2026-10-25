package br.ufal.ic.p2.wepayu.exceptions;

public class ValorNumericoException extends Exception {
    public ValorNumericoException() {
        super("Valor deve ser numerico.");
    }

    public ValorNumericoException(String mensagem) {
        super(mensagem);
    }
}
