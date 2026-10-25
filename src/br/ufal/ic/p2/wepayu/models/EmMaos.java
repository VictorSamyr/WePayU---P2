package br.ufal.ic.p2.wepayu.models;

public class EmMaos extends MetodoPagamento {
    @Override
    public String getCodigo() {
        return "emMaos";
    }

    @Override
    public String getDescricao(String endereco) {
        return "Em maos";
    }
}
