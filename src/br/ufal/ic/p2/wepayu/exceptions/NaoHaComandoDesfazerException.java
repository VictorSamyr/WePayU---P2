package br.ufal.ic.p2.wepayu.exceptions;

public class NaoHaComandoDesfazerException extends Exception {
    public NaoHaComandoDesfazerException() {
        super("Nao ha comando a desfazer.");
    }

    public NaoHaComandoDesfazerException(String mensagem) {
        super(mensagem);
    }
}
