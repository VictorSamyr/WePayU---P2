package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Guarda os dados e os lancamentos de um empregado. */
public class Empregado implements Serializable {
    private final String id;
    private String nome;
    private String endereco;
    private String tipo;
    private BigDecimal salario;
    private BigDecimal comissao;

    private String metodoPagamento = "emMaos";
    private String banco;
    private String agencia;
    private String contaCorrente;

    private boolean sindicalizado;
    private String idSindicato;
    private BigDecimal taxaSindical = BigDecimal.ZERO;

    private LocalDate ultimoPagamento;
    private BigDecimal debitoSindical = BigDecimal.ZERO;

    private final List<Registro> cartoes = new ArrayList<>();
    private final List<Registro> vendas = new ArrayList<>();
    private final List<Registro> taxasServico = new ArrayList<>();

    public Empregado(String id, String nome, String endereco, String tipo,
                     BigDecimal salario, BigDecimal comissao) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.comissao = comissao;
    }

    public Empregado(Empregado outro) {
        id = outro.id;
        nome = outro.nome;
        endereco = outro.endereco;
        tipo = outro.tipo;
        salario = outro.salario;
        comissao = outro.comissao;
        metodoPagamento = outro.metodoPagamento;
        banco = outro.banco;
        agencia = outro.agencia;
        contaCorrente = outro.contaCorrente;
        sindicalizado = outro.sindicalizado;
        idSindicato = outro.idSindicato;
        taxaSindical = outro.taxaSindical;
        ultimoPagamento = outro.ultimoPagamento;
        debitoSindical = outro.debitoSindical;
        cartoes.addAll(outro.cartoes);
        vendas.addAll(outro.vendas);
        taxasServico.addAll(outro.taxasServico);
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public BigDecimal getComissao() { return comissao; }
    public void setComissao(BigDecimal comissao) { this.comissao = comissao; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }
    public boolean isSindicalizado() { return sindicalizado; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }
    public String getIdSindicato() { return idSindicato; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public BigDecimal getTaxaSindical() { return taxaSindical; }
    public void setTaxaSindical(BigDecimal taxaSindical) { this.taxaSindical = taxaSindical; }
    public LocalDate getUltimoPagamento() { return ultimoPagamento; }
    public void setUltimoPagamento(LocalDate ultimoPagamento) { this.ultimoPagamento = ultimoPagamento; }
    public BigDecimal getDebitoSindical() { return debitoSindical; }
    public void setDebitoSindical(BigDecimal debitoSindical) { this.debitoSindical = debitoSindical; }
    public List<Registro> getCartoes() { return cartoes; }
    public List<Registro> getVendas() { return vendas; }
    public List<Registro> getTaxasServico() { return taxasServico; }
}
