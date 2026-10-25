package br.ufal.ic.p2.wepayu.exceptions;

public class EnderecoNuloException extends Exception {
    public EnderecoNuloException() {
        super("Endereco nao pode ser nulo.");
    }

    public EnderecoNuloException(String mensagem) {
        super(mensagem);
    }
}
