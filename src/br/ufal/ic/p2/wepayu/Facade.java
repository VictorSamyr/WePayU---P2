package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EstadoSistema;
import br.ufal.ic.p2.wepayu.models.Registro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/** Fachada do projeto usada pelos testes de aceitacao da disciplina. */
public class Facade {
    private static final Path ARQUIVO_DADOS = Path.of("wepayu.dat");
    private static final DateTimeFormatter FORMATO_DATA = new DateTimeFormatterBuilder()
            .parseStrict().appendPattern("d/M/uuuu").toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);
    private static final LocalDate PRIMEIRA_SEXTA_COMISSIONADOS = LocalDate.of(2005, 1, 14);
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final String LINHA = "=".repeat(127);
    private static final String NL = System.lineSeparator();

    private EstadoSistema estado;
    // Guarda os estados anteriores para fazer undo e redo.
    private final Deque<EstadoSistema> desfazer = new ArrayDeque<>();
    private final Deque<EstadoSistema> refazer = new ArrayDeque<>();
    private boolean encerrado;

    public Facade() {
        estado = carregar();
    }

    public void zerarSistema() throws Exception {
        verificarAtivo();
        registrarAlteracao();
        estado = new EstadoSistema();
    }

    public void encerrarSistema() throws Exception {
        verificarAtivo();
        salvar();
        encerrado = true;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        verificarAtivo();
        validarTexto(nome, "Nome nao pode ser nulo.");
        validarTexto(endereco, "Endereco nao pode ser nulo.");
        validarTipo(tipo);
        if ("comissionado".equals(tipo)) throw new Exception("Tipo nao aplicavel.");
        return adicionarEmpregado(nome, endereco, tipo, validarDinheiro(salario, "Salario"), null);
    }

    public String criarEmpregado(String nome, String endereco, String tipo,
                                 String salario, String comissao) throws Exception {
        verificarAtivo();
        validarTexto(nome, "Nome nao pode ser nulo.");
        validarTexto(endereco, "Endereco nao pode ser nulo.");
        validarTipo(tipo);
        if (!"comissionado".equals(tipo)) throw new Exception("Tipo nao aplicavel.");
        BigDecimal valorSalario = validarDinheiro(salario, "Salario");
        BigDecimal valorComissao = validarDinheiro(comissao, "Comissao");
        return adicionarEmpregado(nome, endereco, tipo, valorSalario, valorComissao);
    }

    private String adicionarEmpregado(String nome, String endereco, String tipo,
                                      BigDecimal salario, BigDecimal comissao) {
        registrarAlteracao();
        String id = estado.novoId();
        estado.getEmpregados().put(id, new Empregado(id, nome, endereco, tipo, salario, comissao));
        return id;
    }

    public void removerEmpregado(String emp) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        registrarAlteracao();
        estado.getEmpregados().remove(e.getId());
    }

    public int getNumeroDeEmpregados() throws Exception {
        verificarAtivo();
        return estado.getEmpregados().size();
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        verificarAtivo();
        int atual = 0;
        for (Empregado e : estado.getEmpregados().values()) {
            if (e.getNome().contains(nome) && ++atual == indice) return e.getId();
        }
        throw new Exception("Nao ha empregado com esse nome.");
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        return switch (atributo) {
            case "nome" -> e.getNome();
            case "endereco" -> e.getEndereco();
            case "tipo" -> e.getTipo();
            case "salario" -> dinheiro(e.getSalario());
            case "comissao" -> {
                exigirTipo(e, "comissionado", "Empregado nao eh comissionado.");
                yield dinheiro(e.getComissao());
            }
            case "metodoPagamento" -> e.getMetodoPagamento();
            case "banco" -> atributoBanco(e, e.getBanco());
            case "agencia" -> atributoBanco(e, e.getAgencia());
            case "contaCorrente" -> atributoBanco(e, e.getContaCorrente());
            case "sindicalizado" -> String.valueOf(e.isSindicalizado());
            case "idSindicato" -> atributoSindicato(e, e.getIdSindicato());
            case "taxaSindical" -> atributoSindicato(e, dinheiro(e.getTaxaSindical()));
            default -> throw new Exception("Atributo nao existe.");
        };
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        switch (atributo) {
            case "nome" -> {
                validarTexto(valor, "Nome nao pode ser nulo.");
                registrarAlteracao();
                estado.getEmpregados().get(emp).setNome(valor);
            }
            case "endereco" -> {
                validarTexto(valor, "Endereco nao pode ser nulo.");
                registrarAlteracao();
                estado.getEmpregados().get(emp).setEndereco(valor);
            }
            case "tipo" -> {
                validarTipo(valor);
                if ("comissionado".equals(valor)) throw new Exception("Tipo nao aplicavel.");
                registrarAlteracao();
                estado.getEmpregados().get(emp).setTipo(valor);
            }
            case "salario" -> {
                BigDecimal numero = validarDinheiro(valor, "Salario");
                registrarAlteracao();
                estado.getEmpregados().get(emp).setSalario(numero);
            }
            case "comissao" -> {
                exigirTipo(e, "comissionado", "Empregado nao eh comissionado.");
                BigDecimal numero = validarDinheiro(valor, "Comissao");
                registrarAlteracao();
                estado.getEmpregados().get(emp).setComissao(numero);
            }
            case "metodoPagamento" -> alterarMetodoSimples(emp, valor);
            case "sindicalizado" -> alterarSindicalizacaoSimples(emp, valor);
            default -> throw new Exception("Atributo nao existe.");
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String adicional) throws Exception {
        verificarAtivo();
        empregado(emp);
        if (!"tipo".equals(atributo)) throw new Exception("Atributo nao existe.");
        validarTipo(valor);
        BigDecimal numero = "comissionado".equals(valor)
                ? validarDinheiro(adicional, "Comissao") : validarDinheiro(adicional, "Salario");
        registrarAlteracao();
        Empregado e = estado.getEmpregados().get(emp);
        e.setTipo(valor);
        if ("comissionado".equals(valor)) e.setComissao(numero);
        else e.setSalario(numero);
    }

    public void alteraEmpregado(String emp, String atributo, String valor,
                                String idSindicato, String taxaSindical) throws Exception {
        verificarAtivo();
        empregado(emp);
        if (!"sindicalizado".equals(atributo)) throw new Exception("Atributo nao existe.");
        if (!"true".equals(valor) && !"false".equals(valor)) {
            throw new Exception("Valor deve ser true ou false.");
        }
        if ("false".equals(valor)) {
            alterarSindicalizacaoSimples(emp, valor);
            return;
        }
        validarTexto(idSindicato, "Identificacao do sindicato nao pode ser nula.");
        BigDecimal taxa = validarDinheiro(taxaSindical, "Taxa sindical");
        for (Empregado outro : estado.getEmpregados().values()) {
            if (!outro.getId().equals(emp) && outro.isSindicalizado()
                    && idSindicato.equals(outro.getIdSindicato())) {
                throw new Exception("Ha outro empregado com esta identificacao de sindicato");
            }
        }
        registrarAlteracao();
        Empregado e = estado.getEmpregados().get(emp);
        e.setSindicalizado(true);
        e.setIdSindicato(idSindicato);
        e.setTaxaSindical(taxa);
    }

    public void alteraEmpregado(String emp, String atributo, String valor,
                                String banco, String agencia, String contaCorrente) throws Exception {
        verificarAtivo();
        empregado(emp);
        if (!"metodoPagamento".equals(atributo) || !"banco".equals(valor)) {
            throw new Exception("Metodo de pagamento invalido.");
        }
        validarTexto(banco, "Banco nao pode ser nulo.");
        validarTexto(agencia, "Agencia nao pode ser nulo.");
        validarTexto(contaCorrente, "Conta corrente nao pode ser nulo.");
        registrarAlteracao();
        Empregado e = estado.getEmpregados().get(emp);
        e.setMetodoPagamento("banco");
        e.setBanco(banco);
        e.setAgencia(agencia);
        e.setContaCorrente(contaCorrente);
    }

    private void alterarMetodoSimples(String emp, String metodo) throws Exception {
        if (!"emMaos".equals(metodo) && !"correios".equals(metodo)) {
            throw new Exception("Metodo de pagamento invalido.");
        }
        registrarAlteracao();
        Empregado e = estado.getEmpregados().get(emp);
        e.setMetodoPagamento(metodo);
        e.setBanco(null);
        e.setAgencia(null);
        e.setContaCorrente(null);
    }

    private void alterarSindicalizacaoSimples(String emp, String valor) throws Exception {
        if (!"true".equals(valor) && !"false".equals(valor)) {
            throw new Exception("Valor deve ser true ou false.");
        }
        if ("true".equals(valor)) throw new Exception("Identificacao do sindicato nao pode ser nula.");
        registrarAlteracao();
        Empregado e = estado.getEmpregados().get(emp);
        e.setSindicalizado(false);
        e.setIdSindicato(null);
        e.setTaxaSindical(BigDecimal.ZERO);
        e.setDebitoSindical(BigDecimal.ZERO);
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        exigirTipo(e, "horista", "Empregado nao eh horista.");
        LocalDate dia = data(data, "Data invalida.");
        BigDecimal quantidade = numero(horas, "Horas devem ser numericas.");
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Horas devem ser positivas.");
        registrarAlteracao();
        estado.getEmpregados().get(emp).getCartoes().add(new Registro(dia, quantidade));
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        exigirTipo(e, "horista", "Empregado nao eh horista.");
        LocalDate[] datas = intervalo(dataInicial, dataFinal);
        BigDecimal total = BigDecimal.ZERO;
        for (Registro cartao : e.getCartoes()) {
            if (estaNoIntervalo(cartao, datas[0], datas[1])) {
                total = total.add(cartao.getValor().min(new BigDecimal("8")));
            }
        }
        return decimalLivre(total);
    }

    public String getHorasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        exigirTipo(e, "horista", "Empregado nao eh horista.");
        LocalDate[] datas = intervalo(dataInicial, dataFinal);
        BigDecimal total = BigDecimal.ZERO;
        for (Registro cartao : e.getCartoes()) {
            if (estaNoIntervalo(cartao, datas[0], datas[1])) {
                total = total.add(cartao.getValor().subtract(new BigDecimal("8")).max(BigDecimal.ZERO));
            }
        }
        return decimalLivre(total);
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        exigirTipo(e, "comissionado", "Empregado nao eh comissionado.");
        LocalDate dia = data(data, "Data invalida.");
        BigDecimal quantia = numero(valor, "Valor deve ser numerico.");
        if (quantia.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Valor deve ser positivo.");
        registrarAlteracao();
        estado.getEmpregados().get(emp).getVendas().add(new Registro(dia, quantia));
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        exigirTipo(e, "comissionado", "Empregado nao eh comissionado.");
        LocalDate[] datas = intervalo(dataInicial, dataFinal);
        return dinheiro(somar(e.getVendas(), datas[0], datas[1]));
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        verificarAtivo();
        if (membro == null || membro.isEmpty()) {
            throw new Exception("Identificacao do membro nao pode ser nula.");
        }
        Empregado encontrado = null;
        for (Empregado e : estado.getEmpregados().values()) {
            if (e.isSindicalizado() && membro.equals(e.getIdSindicato())) {
                encontrado = e;
                break;
            }
        }
        if (encontrado == null) throw new Exception("Membro nao existe.");
        LocalDate dia = data(data, "Data invalida.");
        BigDecimal quantia = numero(valor, "Valor deve ser numerico.");
        if (quantia.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Valor deve ser positivo.");
        String id = encontrado.getId();
        registrarAlteracao();
        estado.getEmpregados().get(id).getTaxasServico().add(new Registro(dia, quantia));
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        verificarAtivo();
        Empregado e = empregado(emp);
        if (!e.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
        LocalDate[] datas = intervalo(dataInicial, dataFinal);
        return dinheiro(somar(e.getTaxasServico(), datas[0], datas[1]));
    }

    public String totalFolha(String data) throws Exception {
        verificarAtivo();
        LocalDate dia = data(data, "Data invalida.");
        BigDecimal total = BigDecimal.ZERO;
        for (Empregado e : estado.getEmpregados().values()) {
            if (deveReceber(e, dia)) total = total.add(calcular(e, dia).bruto);
        }
        return dinheiro(total);
    }

    public void rodaFolha(String data, String saida) throws Exception {
        verificarAtivo();
        LocalDate dia = data(data, "Data invalida.");
        EstadoSistema novoEstado = estado.copiar();
        String relatorio = novoEstado.getFolhasGeradas().get(dia);
        if (relatorio == null) {
            List<LinhaPagamento> pagamentos = calcularFolha(novoEstado, dia);
            relatorio = montarRelatorio(dia, pagamentos);
            efetivarPagamentos(pagamentos, dia);
            novoEstado.getFolhasGeradas().put(dia, relatorio);
        }
        try {
            Files.writeString(Path.of(saida), relatorio, StandardCharsets.UTF_8);
        } catch (IOException erro) {
            throw new Exception("Nao foi possivel escrever arquivo de saida.");
        }
        registrarAlteracao();
        estado = novoEstado;
    }

    public void undo() throws Exception {
        verificarAtivo();
        if (desfazer.isEmpty()) throw new Exception("Nao ha comando a desfazer.");
        refazer.push(estado.copiar());
        estado = desfazer.pop();
    }

    public void redo() throws Exception {
        verificarAtivo();
        if (refazer.isEmpty()) throw new Exception("Nao ha comando a refazer.");
        desfazer.push(estado.copiar());
        estado = refazer.pop();
    }

    private List<LinhaPagamento> calcularFolha(EstadoSistema sistema, LocalDate dia) {
        List<Empregado> ordenados = new ArrayList<>(sistema.getEmpregados().values());
        ordenados.sort(Comparator.comparing(Empregado::getNome));
        List<LinhaPagamento> linhas = new ArrayList<>();
        for (Empregado e : ordenados) if (deveReceber(e, dia)) linhas.add(calcular(e, dia));
        return linhas;
    }

    private LinhaPagamento calcular(Empregado e, LocalDate dia) {
        LinhaPagamento linha = new LinhaPagamento(e);
        LocalDate inicioPeriodo;
        LocalDate fimExclusivo = dia.plusDays(1);

        // O calculo muda de acordo com o tipo do empregado.
        if ("horista".equals(e.getTipo())) {
            inicioPeriodo = dia.minusDays(6);
            for (Registro cartao : e.getCartoes()) {
                if (estaNoIntervalo(cartao, inicioPeriodo, fimExclusivo)) {
                    BigDecimal horas = cartao.getValor();
                    linha.horasNormais = linha.horasNormais.add(horas.min(new BigDecimal("8")));
                    linha.horasExtras = linha.horasExtras.add(horas.subtract(new BigDecimal("8")).max(BigDecimal.ZERO));
                }
            }
            linha.bruto = centavos(linha.horasNormais.multiply(e.getSalario())
                    .add(linha.horasExtras.multiply(e.getSalario()).multiply(new BigDecimal("1.5"))));
        } else if ("comissionado".equals(e.getTipo())) {
            inicioPeriodo = dia.minusDays(13);
            linha.fixo = centavos(e.getSalario().multiply(new BigDecimal("12"))
                    .divide(new BigDecimal("26"), 10, RoundingMode.HALF_EVEN));
            linha.vendas = centavos(somar(e.getVendas(), inicioPeriodo, fimExclusivo));
            linha.comissao = centavos(linha.vendas.multiply(e.getComissao()));
            linha.bruto = centavos(linha.fixo.add(linha.comissao));
        } else {
            inicioPeriodo = dia.withDayOfMonth(1);
            linha.bruto = centavos(e.getSalario());
        }
        if (e.isSindicalizado() && linha.bruto.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate inicioDeducoes = e.getUltimoPagamento() == null
                    ? inicioInicialDeducoes(e, inicioPeriodo) : e.getUltimoPagamento().plusDays(1);
            long dias = inicioDeducoes.isAfter(dia) ? 0 : ChronoUnit.DAYS.between(inicioDeducoes, dia) + 1;
            BigDecimal mensalidade = e.getTaxaSindical().multiply(BigDecimal.valueOf(dias));
            BigDecimal servicos = somar(e.getTaxasServico(), inicioDeducoes, fimExclusivo);
            linha.deducoesCalculadas = centavos(e.getDebitoSindical().add(mensalidade).add(servicos));
            linha.descontos = linha.deducoesCalculadas.min(linha.bruto);
        }
        linha.liquido = centavos(linha.bruto.subtract(linha.descontos).max(BigDecimal.ZERO));
        return linha;
    }

    private LocalDate inicioInicialDeducoes(Empregado e, LocalDate inicioPeriodo) {
        if (!"horista".equals(e.getTipo()) || e.getCartoes().isEmpty()) return inicioPeriodo;
        LocalDate primeiro = e.getCartoes().stream().map(Registro::getData)
                .min(LocalDate::compareTo).orElse(inicioPeriodo);
        return primeiro.isAfter(inicioPeriodo) ? primeiro : inicioPeriodo;
    }

    private void efetivarPagamentos(List<LinhaPagamento> pagamentos, LocalDate dia) {
        for (LinhaPagamento p : pagamentos) {
            if (p.bruto.compareTo(BigDecimal.ZERO) > 0) {
                p.empregado.setUltimoPagamento(dia);
                p.empregado.setDebitoSindical(p.deducoesCalculadas.subtract(p.descontos).max(BigDecimal.ZERO));
            }
        }
    }

    // O formato precisa ser igual ao dos arquivos usados nos testes de aceitacao.
    private String montarRelatorio(LocalDate dia, List<LinhaPagamento> pagamentos) {
        String titulo = "FOLHA DE PAGAMENTO DO DIA " + dia;
        StringBuilder texto = new StringBuilder();
        texto.append(titulo).append(NL).append("=".repeat(titulo.length())).append(NL).append(NL);
        texto.append(LINHA).append(NL)
                .append("===================== HORISTAS ================================================================================================").append(NL)
                .append(LINHA).append(NL)
                .append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo").append(NL)
                .append("==================================== ===== ===== ============= ========= =============== ======================================").append(NL);
        BigDecimal totalHoras = BigDecimal.ZERO, totalExtras = BigDecimal.ZERO;
        BigDecimal brutoH = BigDecimal.ZERO, descontosH = BigDecimal.ZERO, liquidoH = BigDecimal.ZERO;
        for (LinhaPagamento p : pagamentos) {
            if (!"horista".equals(p.empregado.getTipo())) continue;
            texto.append(String.format("%-36s %5s %5s %13s %9s %15s %s", p.empregado.getNome(),
                    decimalLivre(p.horasNormais), decimalLivre(p.horasExtras), dinheiro(p.bruto),
                    dinheiro(p.descontos), dinheiro(p.liquido), descricaoPagamento(p.empregado))).append(NL);
            totalHoras = totalHoras.add(p.horasNormais); totalExtras = totalExtras.add(p.horasExtras);
            brutoH = brutoH.add(p.bruto); descontosH = descontosH.add(p.descontos); liquidoH = liquidoH.add(p.liquido);
        }
        texto.append(NL).append(String.format("%-36s %5s %5s %13s %9s %15s", "TOTAL HORISTAS",
                decimalLivre(totalHoras), decimalLivre(totalExtras), dinheiro(brutoH), dinheiro(descontosH), dinheiro(liquidoH)))
                .append(NL).append(NL);
        texto.append(LINHA).append(NL)
                .append("===================== ASSALARIADOS ============================================================================================").append(NL)
                .append(LINHA).append(NL)
                .append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo").append(NL)
                .append("================================================ ============= ========= =============== ======================================").append(NL);
        BigDecimal brutoA = BigDecimal.ZERO, descontosA = BigDecimal.ZERO, liquidoA = BigDecimal.ZERO;
        for (LinhaPagamento p : pagamentos) {
            if (!"assalariado".equals(p.empregado.getTipo())) continue;
            texto.append(String.format("%-48s %13s %9s %15s %s", p.empregado.getNome(), dinheiro(p.bruto),
                    dinheiro(p.descontos), dinheiro(p.liquido), descricaoPagamento(p.empregado))).append(NL);
            brutoA = brutoA.add(p.bruto); descontosA = descontosA.add(p.descontos); liquidoA = liquidoA.add(p.liquido);
        }
        texto.append(NL).append(String.format("%-48s %13s %9s %15s", "TOTAL ASSALARIADOS",
                dinheiro(brutoA), dinheiro(descontosA), dinheiro(liquidoA))).append(NL).append(NL);
        texto.append(LINHA).append(NL)
                .append("===================== COMISSIONADOS ===========================================================================================").append(NL)
                .append(LINHA).append(NL)
                .append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo").append(NL)
                .append("===================== ======== ======== ======== ============= ========= =============== ======================================").append(NL);
        BigDecimal fixo = BigDecimal.ZERO, vendas = BigDecimal.ZERO, comissao = BigDecimal.ZERO;
        BigDecimal brutoC = BigDecimal.ZERO, descontosC = BigDecimal.ZERO, liquidoC = BigDecimal.ZERO;
        for (LinhaPagamento p : pagamentos) {
            if (!"comissionado".equals(p.empregado.getTipo())) continue;
            texto.append(String.format("%-21s %8s %8s %8s %13s %9s %15s %s", p.empregado.getNome(),
                    dinheiro(p.fixo), dinheiro(p.vendas), dinheiro(p.comissao), dinheiro(p.bruto),
                    dinheiro(p.descontos), dinheiro(p.liquido), descricaoPagamento(p.empregado))).append(NL);
            fixo = fixo.add(p.fixo); vendas = vendas.add(p.vendas); comissao = comissao.add(p.comissao);
            brutoC = brutoC.add(p.bruto); descontosC = descontosC.add(p.descontos); liquidoC = liquidoC.add(p.liquido);
        }
        texto.append(NL).append(String.format("%-21s %8s %8s %8s %13s %9s %15s", "TOTAL COMISSIONADOS",
                dinheiro(fixo), dinheiro(vendas), dinheiro(comissao), dinheiro(brutoC), dinheiro(descontosC), dinheiro(liquidoC)))
                .append(NL).append(NL);
        texto.append("TOTAL FOLHA: ").append(dinheiro(brutoH.add(brutoA).add(brutoC))).append(NL);
        return texto.toString();
    }

    private boolean deveReceber(Empregado e, LocalDate dia) {
        return switch (e.getTipo()) {
            case "horista" -> dia.getDayOfWeek() == DayOfWeek.FRIDAY;
            case "comissionado" -> dia.getDayOfWeek() == DayOfWeek.FRIDAY
                    && Math.floorMod(ChronoUnit.DAYS.between(PRIMEIRA_SEXTA_COMISSIONADOS, dia), 14) == 0;
            case "assalariado" -> dia.equals(ultimoDiaUtil(dia));
            default -> false;
        };
    }

    private LocalDate ultimoDiaUtil(LocalDate dia) {
        LocalDate ultimo = dia.withDayOfMonth(dia.lengthOfMonth());
        if (ultimo.getDayOfWeek() == DayOfWeek.SATURDAY) return ultimo.minusDays(1);
        if (ultimo.getDayOfWeek() == DayOfWeek.SUNDAY) return ultimo.minusDays(2);
        return ultimo;
    }

    private String descricaoPagamento(Empregado e) {
        return switch (e.getMetodoPagamento()) {
            case "correios" -> "Correios, " + e.getEndereco();
            case "banco" -> e.getBanco() + ", Ag. " + e.getAgencia() + " CC " + e.getContaCorrente();
            default -> "Em maos";
        };
    }

    private Empregado empregado(String id) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = estado.getEmpregados().get(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        return e;
    }

    private String atributoBanco(Empregado e, String valor) throws Exception {
        if (!"banco".equals(e.getMetodoPagamento())) throw new Exception("Empregado nao recebe em banco.");
        return valor;
    }

    private String atributoSindicato(Empregado e, String valor) throws Exception {
        if (!e.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
        return valor;
    }

    private void exigirTipo(Empregado e, String tipo, String mensagem) throws Exception {
        if (!tipo.equals(e.getTipo())) throw new Exception(mensagem);
    }

    private void validarTipo(String tipo) throws Exception {
        if (!"horista".equals(tipo) && !"assalariado".equals(tipo) && !"comissionado".equals(tipo)) {
            throw new Exception("Tipo invalido.");
        }
    }

    private void validarTexto(String valor, String mensagem) throws Exception {
        if (valor == null || valor.isEmpty()) throw new Exception(mensagem);
    }

    private BigDecimal validarDinheiro(String valor, String campo) throws Exception {
        boolean feminino = "Comissao".equals(campo) || "Taxa sindical".equals(campo);
        String generoNulo = feminino ? "nula" : "nulo";
        String generoNumerico = feminino ? "numerica" : "numerico";
        String generoNegativo = feminino ? "nao-negativa" : "nao-negativo";
        if (valor == null || valor.isEmpty()) throw new Exception(campo + " nao pode ser " + generoNulo + ".");
        BigDecimal n = numero(valor, campo + " deve ser " + generoNumerico + ".");
        if (n.compareTo(BigDecimal.ZERO) < 0) throw new Exception(campo + " deve ser " + generoNegativo + ".");
        return n;
    }

    private BigDecimal numero(String valor, String mensagem) throws Exception {
        try { return new BigDecimal(valor.replace(',', '.')); }
        catch (Exception erro) { throw new Exception(mensagem); }
    }

    private LocalDate data(String valor, String mensagem) throws Exception {
        try { return LocalDate.parse(valor, FORMATO_DATA); }
        catch (DateTimeParseException | NullPointerException erro) { throw new Exception(mensagem); }
    }

    private LocalDate[] intervalo(String inicio, String fim) throws Exception {
        LocalDate dataInicial = data(inicio, "Data inicial invalida.");
        LocalDate dataFinal = data(fim, "Data final invalida.");
        if (dataInicial.isAfter(dataFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        return new LocalDate[]{dataInicial, dataFinal};
    }

    private boolean estaNoIntervalo(Registro registro, LocalDate inicio, LocalDate fimExclusivo) {
        return !registro.getData().isBefore(inicio) && registro.getData().isBefore(fimExclusivo);
    }

    private BigDecimal somar(List<Registro> registros, LocalDate inicio, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (Registro r : registros) if (estaNoIntervalo(r, inicio, fimExclusivo)) total = total.add(r.getValor());
        return total;
    }

    // Neste projeto da disciplina, os valores monetarios sao truncados.
    private BigDecimal centavos(BigDecimal valor) { return valor.setScale(2, RoundingMode.DOWN); }
    private String dinheiro(BigDecimal valor) { return centavos(valor).toPlainString().replace('.', ','); }
    private String decimalLivre(BigDecimal valor) {
        BigDecimal n = valor.stripTrailingZeros();
        if (n.scale() < 0) n = n.setScale(0);
        return n.toPlainString().replace('.', ',');
    }

    private void verificarAtivo() throws Exception {
        if (encerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
    }

    private void registrarAlteracao() {
        // Depois de um comando novo, nao ha mais o que refazer.
        desfazer.push(estado.copiar());
        refazer.clear();
    }

    private EstadoSistema carregar() {
        if (!Files.exists(ARQUIVO_DADOS)) return new EstadoSistema();
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(ARQUIVO_DADOS))) {
            return (EstadoSistema) entrada.readObject();
        } catch (Exception erro) {
            return new EstadoSistema();
        }
    }

    private void salvar() throws Exception {
        try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(ARQUIVO_DADOS))) {
            saida.writeObject(estado);
        } catch (IOException erro) {
            throw new Exception("Nao foi possivel salvar o sistema.");
        }
    }

    private static class LinhaPagamento {
        private final Empregado empregado;
        private BigDecimal horasNormais = BigDecimal.ZERO;
        private BigDecimal horasExtras = BigDecimal.ZERO;
        private BigDecimal fixo = ZERO;
        private BigDecimal vendas = ZERO;
        private BigDecimal comissao = ZERO;
        private BigDecimal bruto = ZERO;
        private BigDecimal descontos = ZERO;
        private BigDecimal deducoesCalculadas = ZERO;
        private BigDecimal liquido = ZERO;

        private LinhaPagamento(Empregado empregado) { this.empregado = empregado; }
    }
}
