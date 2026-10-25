package br.ufal.ic.p2.wepayu.models;

public class Correios extends MetodoPagamento {
    @Override
    public String getCodigo() {
        return "correios";
    }

    @Override
    public String getDescricao(String endereco) {
        return "Correios, " + endereco;
    }
}
