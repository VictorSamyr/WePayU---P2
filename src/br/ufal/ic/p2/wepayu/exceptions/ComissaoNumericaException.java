package br.ufal.ic.p2.wepayu.exceptions;

public class ComissaoNumericaException extends Exception {
    public ComissaoNumericaException() {
        super("Comissao deve ser numerica.");
    }

    public ComissaoNumericaException(String mensagem) {
        super(mensagem);
    }
}
