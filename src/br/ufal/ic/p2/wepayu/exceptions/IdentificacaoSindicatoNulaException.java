package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoSindicatoNulaException extends Exception {
    public IdentificacaoSindicatoNulaException() {
        super("Identificacao do sindicato nao pode ser nula.");
    }

    public IdentificacaoSindicatoNulaException(String mensagem) {
        super(mensagem);
    }
}
