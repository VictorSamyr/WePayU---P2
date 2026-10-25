package br.ufal.ic.p2.wepayu.exceptions;

public class NaoHaComandoRefazerException extends Exception {
    public NaoHaComandoRefazerException() {
        super("Nao ha comando a refazer.");
    }

    public NaoHaComandoRefazerException(String mensagem) {
        super(mensagem);
    }
}
