package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.models.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Servico para gerenciamento do ciclo de vida, dados e lancamentos de empregados.
 */
public class EmpregadoService {
    private static final DateTimeFormatter FORMATO_DATA = new DateTimeFormatterBuilder()
            .parseStrict().appendPattern("d/M/uuuu").toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    private final AgendaService agendaService;
    private final SindicatoService sindicatoService;

    public EmpregadoService(AgendaService agendaService, SindicatoService sindicatoService) {
        this.agendaService = agendaService;
        this.sindicatoService = sindicatoService;
    }

    public Empregado buscarEmpregado(EstadoSistema estado, String id) throws Exception {
        if (id == null || id.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        Empregado e = estado.getEmpregados().get(id);
        if (e == null) {
            throw new EmpregadoNaoExisteException();
        }
        return e;
    }

    public int getNumeroDeEmpregados(EstadoSistema estado) {
        return estado.getEmpregados().size();
    }

    public String getEmpregadoPorNome(EstadoSistema estado, String nome, int indice) throws Exception {
        int atual = 0;
        for (Empregado e : estado.getEmpregados().values()) {
            if (e.getNome().contains(nome) && ++atual == indice) {
                return e.getId();
            }
        }
        throw new NaoHaEmpregadoComEsseNomeException();
    }

    public String criarEmpregado(EstadoSistema estado, String nome, String endereco,
                                 String tipo, String salario) throws Exception {
        validarTexto(nome, new NomeNuloException());
        validarTexto(endereco, new EnderecoNuloException());
        validarTipo(tipo);
        if ("comissionado".equals(tipo)) {
            throw new TipoNaoAplicavelException();
        }
        BigDecimal valorSalario = validarSalario(salario);
        String id = estado.novoId();
        AgendaPagamento agenda = agendaService.getAgendaDefault(estado, tipo);
        Empregado novo;
        if ("horista".equals(tipo)) {
            novo = new EmpregadoHorista(id, nome, endereco, valorSalario, agenda);
        } else {
            novo = new EmpregadoAssalariado(id, nome, endereco, valorSalario, agenda);
        }
        estado.getEmpregados().put(id, novo);
        return id;
    }

    public String criarEmpregado(EstadoSistema estado, String nome, String endereco,
                                 String tipo, String salario, String comissao) throws Exception {
        validarTexto(nome, new NomeNuloException());
        validarTexto(endereco, new EnderecoNuloException());
        validarTipo(tipo);
        if (!"comissionado".equals(tipo)) {
            throw new TipoNaoAplicavelException();
        }
        BigDecimal valorSalario = validarSalario(salario);
        BigDecimal valorComissao = validarComissao(comissao);
        String id = estado.novoId();
        AgendaPagamento agenda = agendaService.getAgendaDefault(estado, tipo);
        Empregado novo = new EmpregadoComissionado(id, nome, endereco, valorSalario, valorComissao, agenda);
        estado.getEmpregados().put(id, novo);
        return id;
    }

    public void removerEmpregado(EstadoSistema estado, String emp) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        estado.getEmpregados().remove(e.getId());
    }

    public String getAtributoEmpregado(EstadoSistema estado, String emp, String atributo) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        return switch (atributo) {
            case "nome" -> e.getNome();
            case "endereco" -> e.getEndereco();
            case "tipo" -> e.getTipo();
            case "salario" -> dinheiro(e.getSalario());
            case "comissao" -> {
                if (!(e instanceof EmpregadoComissionado comissionado)) {
                    throw new EmpregadoNaoEhComissionadoException();
                }
                yield dinheiro(comissionado.getComissao());
            }
            case "metodoPagamento" -> e.getMetodoPagamento().getCodigo();
            case "banco" -> {
                if (!(e.getMetodoPagamento() instanceof Banco banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                yield banco.getBanco();
            }
            case "agencia" -> {
                if (!(e.getMetodoPagamento() instanceof Banco banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                yield banco.getAgencia();
            }
            case "contaCorrente" -> {
                if (!(e.getMetodoPagamento() instanceof Banco banco)) {
                    throw new EmpregadoNaoRecebeEmBancoException();
                }
                yield banco.getContaCorrente();
            }
            case "sindicalizado" -> String.valueOf(e.isSindicalizado());
            case "idSindicato" -> {
                if (!e.isSindicalizado()) {
                    throw new EmpregadoNaoEhSindicalizadoException();
                }
                yield e.getSindicato().getIdMembro();
            }
            case "taxaSindical" -> {
                if (!e.isSindicalizado()) {
                    throw new EmpregadoNaoEhSindicalizadoException();
                }
                yield dinheiro(e.getSindicato().getTaxaSindical());
            }
            case "agendaPagamento" -> e.getAgenda().getDescricao();
            default -> throw new AtributoNaoExisteException();
        };
    }

    public void alteraEmpregado(EstadoSistema estado, String emp, String atributo, String valor) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        switch (atributo) {
            case "nome" -> {
                validarTexto(valor, new NomeNuloException());
                e.setNome(valor);
            }
            case "endereco" -> {
                validarTexto(valor, new EnderecoNuloException());
                e.setEndereco(valor);
            }
            case "tipo" -> {
                validarTipo(valor);
                if ("comissionado".equals(valor)) {
                    throw new TipoNaoAplicavelException();
                }
                converterTipoEmpregado(estado, e, valor, e.getSalario(), null);
            }
            case "salario" -> {
                BigDecimal novoSalario = validarSalario(valor);
                e.setSalario(novoSalario);
            }
            case "comissao" -> {
                if (!(e instanceof EmpregadoComissionado comissionado)) {
                    throw new EmpregadoNaoEhComissionadoException();
                }
                BigDecimal novaComissao = validarComissao(valor);
                comissionado.setComissao(novaComissao);
            }
            case "metodoPagamento" -> {
                if ("emMaos".equals(valor)) {
                    e.setMetodoPagamento(new EmMaos());
                } else if ("correios".equals(valor)) {
                    e.setMetodoPagamento(new Correios());
                } else {
                    throw new MetodoPagamentoInvalidoException();
                }
            }
            case "sindicalizado" -> {
                if (!"true".equals(valor) && !"false".equals(valor)) {
                    throw new ValorBooleanoInvalidoException();
                }
                if ("true".equals(valor)) {
                    throw new IdentificacaoSindicatoNulaException();
                }
                e.setSindicato(null);
            }
            case "agendaPagamento" -> {
                AgendaPagamento agenda = agendaService.buscarAgenda(estado, valor);
                e.setAgenda(agenda);
            }
            default -> throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(EstadoSistema estado, String emp, String atributo,
                                String valor, String adicional) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!"tipo".equals(atributo)) {
            throw new AtributoNaoExisteException();
        }
        validarTipo(valor);
        if ("comissionado".equals(valor)) {
            BigDecimal comissao = validarComissao(adicional);
            converterTipoEmpregado(estado, e, valor, e.getSalario(), comissao);
        } else {
            BigDecimal salario = validarSalario(adicional);
            converterTipoEmpregado(estado, e, valor, salario, null);
        }
    }

    public void alteraEmpregado(EstadoSistema estado, String emp, String atributo, String valor,
                                String idSindicato, String taxaSindical) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!"sindicalizado".equals(atributo)) {
            throw new AtributoNaoExisteException();
        }
        if (!"true".equals(valor) && !"false".equals(valor)) {
            throw new ValorBooleanoInvalidoException();
        }
        if ("false".equals(valor)) {
            e.setSindicato(null);
            return;
        }
        if (idSindicato == null || idSindicato.isEmpty()) {
            throw new IdentificacaoSindicatoNulaException();
        }
        BigDecimal taxa = validarTaxaSindical(taxaSindical);
        sindicatoService.validarIdentificacaoUnica(estado, emp, idSindicato);
        e.setSindicato(new MembroSindicato(idSindicato, taxa));
    }

    public void alteraEmpregado(EstadoSistema estado, String emp, String atributo, String valor,
                                String banco, String agencia, String contaCorrente) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!"metodoPagamento".equals(atributo) || !"banco".equals(valor)) {
            throw new MetodoPagamentoInvalidoException();
        }
        validarTexto(banco, new BancoNuloException());
        validarTexto(agencia, new AgenciaNulaException());
        validarTexto(contaCorrente, new ContaCorrenteNulaException());
        e.setMetodoPagamento(new Banco(banco, agencia, contaCorrente));
    }

    private void converterTipoEmpregado(EstadoSistema estado, Empregado e, String novoTipo,
                                        BigDecimal salario, BigDecimal comissao) {
        Empregado novo;
        if ("horista".equals(novoTipo)) {
            novo = new EmpregadoHorista(e.getId(), e.getNome(), e.getEndereco(), salario, e.getAgenda());
        } else if ("comissionado".equals(novoTipo)) {
            novo = new EmpregadoComissionado(e.getId(), e.getNome(), e.getEndereco(), salario, comissao, e.getAgenda());
        } else {
            novo = new EmpregadoAssalariado(e.getId(), e.getNome(), e.getEndereco(), salario, e.getAgenda());
        }
        novo.setMetodoPagamento(e.getMetodoPagamento());
        novo.setSindicato(e.getSindicato());
        novo.setUltimoPagamento(e.getUltimoPagamento());
        estado.getEmpregados().put(e.getId(), novo);
    }

    public void lancaCartao(EstadoSistema estado, String emp, String data, String horas) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!(e instanceof EmpregadoHorista horista)) {
            throw new EmpregadoNaoEhHoristaException();
        }
        LocalDate dia = parseData(data, new DataInvalidaException());
        BigDecimal qtdHoras = parseNumero(horas, new HorasNumericasException());
        if (qtdHoras.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HorasPositivasException();
        }
        horista.adicionarCartao(new CartaoDePonto(dia, qtdHoras));
    }

    public String getHorasNormaisTrabalhadas(EstadoSistema estado, String emp,
                                            String dataInicial, String dataFinal) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!(e instanceof EmpregadoHorista horista)) {
            throw new EmpregadoNaoEhHoristaException();
        }
        LocalDate[] datas = validarIntervalo(dataInicial, dataFinal);
        return decimalLivre(horista.getHorasNormais(datas[0], datas[1]));
    }

    public String getHorasExtrasTrabalhadas(EstadoSistema estado, String emp,
                                           String dataInicial, String dataFinal) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!(e instanceof EmpregadoHorista horista)) {
            throw new EmpregadoNaoEhHoristaException();
        }
        LocalDate[] datas = validarIntervalo(dataInicial, dataFinal);
        return decimalLivre(horista.getHorasExtras(datas[0], datas[1]));
    }

    public void lancaVenda(EstadoSistema estado, String emp, String data, String valor) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!(e instanceof EmpregadoComissionado comissionado)) {
            throw new EmpregadoNaoEhComissionadoException();
        }
        LocalDate dia = parseData(data, new DataInvalidaException());
        BigDecimal quantia = parseNumero(valor, new ValorNumericoException());
        if (quantia.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorPositivoException();
        }
        comissionado.adicionarVenda(new Venda(dia, quantia));
    }

    public String getVendasRealizadas(EstadoSistema estado, String emp,
                                      String dataInicial, String dataFinal) throws Exception {
        Empregado e = buscarEmpregado(estado, emp);
        if (!(e instanceof EmpregadoComissionado comissionado)) {
            throw new EmpregadoNaoEhComissionadoException();
        }
        LocalDate[] datas = validarIntervalo(dataInicial, dataFinal);
        return dinheiro(comissionado.getVendasNoPeriodo(datas[0], datas[1]));
    }

    private LocalDate[] validarIntervalo(String inicio, String fim) throws Exception {
        LocalDate dataInicial = parseData(inicio, new DataInicialInvalidaException());
        LocalDate dataFinal = parseData(fim, new DataFinalInvalidaException());
        if (dataInicial.isAfter(dataFinal)) {
            throw new DataInicialPosteriorException();
        }
        return new LocalDate[]{dataInicial, dataFinal};
    }

    private void validarTexto(String valor, Exception erro) throws Exception {
        if (valor == null || valor.isEmpty()) {
            throw erro;
        }
    }

    private void validarTipo(String tipo) throws TipoInvalidoException {
        if (!"horista".equals(tipo) && !"assalariado".equals(tipo) && !"comissionado".equals(tipo)) {
            throw new TipoInvalidoException();
        }
    }

    private BigDecimal validarSalario(String valor) throws Exception {
        validarTexto(valor, new SalarioNuloException());
        BigDecimal n = parseNumero(valor, new SalarioNumericoException());
        if (n.compareTo(BigDecimal.ZERO) < 0) {
            throw new SalarioNaoNegativoException();
        }
        return n;
    }

    private BigDecimal validarComissao(String valor) throws Exception {
        validarTexto(valor, new ComissaoNulaException());
        BigDecimal n = parseNumero(valor, new ComissaoNumericaException());
        if (n.compareTo(BigDecimal.ZERO) < 0) {
            throw new ComissaoNaoNegativaException();
        }
        return n;
    }

    private BigDecimal validarTaxaSindical(String valor) throws Exception {
        validarTexto(valor, new TaxaSindicalNulaException());
        BigDecimal n = parseNumero(valor, new TaxaSindicalNumericaException());
        if (n.compareTo(BigDecimal.ZERO) < 0) {
            throw new TaxaSindicalNaoNegativaException();
        }
        return n;
    }

    private LocalDate parseData(String valor, Exception erro) throws Exception {
        try {
            return LocalDate.parse(valor, FORMATO_DATA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw erro;
        }
    }

    private BigDecimal parseNumero(String valor, Exception erro) throws Exception {
        try {
            return new BigDecimal(valor.replace(',', '.'));
        } catch (Exception e) {
            throw erro;
        }
    }

    public static String dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }

    public static String decimalLivre(BigDecimal valor) {
        BigDecimal n = valor.stripTrailingZeros();
        if (n.scale() < 0) {
            n = n.setScale(0);
        }
        return n.toPlainString().replace('.', ',');
    }
}
