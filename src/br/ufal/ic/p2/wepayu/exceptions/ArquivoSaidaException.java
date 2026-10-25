package br.ufal.ic.p2.wepayu.exceptions;

public class ArquivoSaidaException extends Exception {
    public ArquivoSaidaException() {
        super("Nao foi possivel escrever arquivo de saida.");
    }

    public ArquivoSaidaException(String mensagem) {
        super(mensagem);
    }
}
