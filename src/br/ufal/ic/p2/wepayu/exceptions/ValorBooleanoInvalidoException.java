package br.ufal.ic.p2.wepayu.exceptions;

public class ValorBooleanoInvalidoException extends Exception {
    public ValorBooleanoInvalidoException() {
        super("Valor deve ser true ou false.");
    }

    public ValorBooleanoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
