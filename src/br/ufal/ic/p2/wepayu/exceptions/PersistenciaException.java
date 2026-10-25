package br.ufal.ic.p2.wepayu.exceptions;

public class PersistenciaException extends Exception {
    public PersistenciaException() {
        super("Nao foi possivel salvar o sistema.");
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
