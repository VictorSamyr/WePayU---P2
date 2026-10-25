package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoSindicatoJaExisteException extends Exception {
    public IdentificacaoSindicatoJaExisteException() {
        super("Ha outro empregado com esta identificacao de sindicato");
    }

    public IdentificacaoSindicatoJaExisteException(String mensagem) {
        super(mensagem);
    }
}
