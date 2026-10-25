package br.ufal.ic.p2.wepayu.exceptions;

public class ComissaoNaoNegativaException extends Exception {
    public ComissaoNaoNegativaException() {
        super("Comissao deve ser nao-negativa.");
    }

    public ComissaoNaoNegativaException(String mensagem) {
        super(mensagem);
    }
}
