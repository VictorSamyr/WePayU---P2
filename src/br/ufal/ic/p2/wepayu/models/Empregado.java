package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Superclasse abstrata para todos os tipos de empregados do sistema.
 */
public abstract class Empregado implements Serializable {
    private final String id;
    private String nome;
    private String endereco;
    private MetodoPagamento metodoPagamento = new EmMaos();
    private MembroSindicato sindicato;
    private AgendaPagamento agenda;
    private LocalDate ultimoPagamento;

    public Empregado(String id, String nome, String endereco, AgendaPagamento agenda) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.agenda = agenda;
    }

    protected Empregado(Empregado outro) {
        this.id = outro.id;
        this.nome = outro.nome;
        this.endereco = outro.endereco;
        this.metodoPagamento = outro.metodoPagamento;
        this.sindicato = outro.sindicato != null ? new MembroSindicato(outro.sindicato) : null;
        this.agenda = outro.agenda;
        this.ultimoPagamento = outro.ultimoPagamento;
    }

    public abstract String getTipo();
    public abstract BigDecimal getSalario();
    public abstract void setSalario(BigDecimal salario);
    public abstract BigDecimal calcularSalarioBruto(LocalDate data);

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public MembroSindicato getSindicato() {
        return sindicato;
    }

    public void setSindicato(MembroSindicato sindicato) {
        this.sindicato = sindicato;
    }

    public boolean isSindicalizado() {
        return sindicato != null;
    }

    public AgendaPagamento getAgenda() {
        return agenda;
    }

    public void setAgenda(AgendaPagamento agenda) {
        this.agenda = agenda;
    }

    public LocalDate getUltimoPagamento() {
        return ultimoPagamento;
    }

    public void setUltimoPagamento(LocalDate ultimoPagamento) {
        this.ultimoPagamento = ultimoPagamento;
    }
}
