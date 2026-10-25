package br.ufal.ic.p2.wepayu.exceptions;

public class TipoNaoAplicavelException extends Exception {
    public TipoNaoAplicavelException() {
        super("Tipo nao aplicavel.");
    }

    public TipoNaoAplicavelException(String mensagem) {
        super(mensagem);
    }
}
