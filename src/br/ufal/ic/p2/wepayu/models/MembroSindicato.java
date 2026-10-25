package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa os dados e lancamentos sindicais de um empregado.
 */
public class MembroSindicato implements Serializable {
    private String idMembro;
    private BigDecimal taxaSindical;
    private BigDecimal debitoSindical;
    private final List<TaxaServico> taxasServico = new ArrayList<>();

    public MembroSindicato(String idMembro, BigDecimal taxaSindical) {
        this.idMembro = idMembro;
        this.taxaSindical = taxaSindical;
        this.debitoSindical = BigDecimal.ZERO;
    }

    public MembroSindicato(MembroSindicato outro) {
        this.idMembro = outro.idMembro;
        this.taxaSindical = outro.taxaSindical;
        this.debitoSindical = outro.debitoSindical;
        this.taxasServico.addAll(outro.taxasServico);
    }

    public String getIdMembro() {
        return idMembro;
    }

    public void setIdMembro(String idMembro) {
        this.idMembro = idMembro;
    }

    public BigDecimal getTaxaSindical() {
        return taxaSindical;
    }

    public void setTaxaSindical(BigDecimal taxaSindical) {
        this.taxaSindical = taxaSindical;
    }

    public BigDecimal getDebitoSindical() {
        return debitoSindical;
    }

    public void setDebitoSindical(BigDecimal debitoSindical) {
        this.debitoSindical = debitoSindical;
    }

    public void adicionarTaxaServico(TaxaServico taxa) {
        taxasServico.add(taxa);
    }

    public List<TaxaServico> getTaxasServico() {
        return Collections.unmodifiableList(taxasServico);
    }
}
