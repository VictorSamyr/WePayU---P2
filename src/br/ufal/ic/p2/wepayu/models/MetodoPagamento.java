package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;

/**
 * Superclasse abstrata para metodos de pagamento.
 */
public abstract class MetodoPagamento implements Serializable {
    public abstract String getCodigo();
    public abstract String getDescricao(String endereco);
}
