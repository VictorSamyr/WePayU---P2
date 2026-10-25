package br.ufal.ic.p2.wepayu.models;

public class Banco extends MetodoPagamento {
    private final String banco;
    private final String agencia;
    private final String contaCorrente;

    public Banco(String banco, String agencia, String contaCorrente) {
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    @Override
    public String getCodigo() {
        return "banco";
    }

    @Override
    public String getDescricao(String endereco) {
        return banco + ", Ag. " + agencia + " CC " + contaCorrente;
    }

    public String getBanco() {
        return banco;
    }

    public String getAgencia() {
        return agencia;
    }

    public String getContaCorrente() {
        return contaCorrente;
    }
}
