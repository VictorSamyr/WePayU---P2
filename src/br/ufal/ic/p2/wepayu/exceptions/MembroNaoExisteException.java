package br.ufal.ic.p2.wepayu.exceptions;

public class MembroNaoExisteException extends Exception {
    public MembroNaoExisteException() {
        super("Membro nao existe.");
    }

    public MembroNaoExisteException(String mensagem) {
        super(mensagem);
    }
}
