package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.exceptions.ArquivoSaidaException;
import br.ufal.ic.p2.wepayu.exceptions.DataInvalidaException;
import br.ufal.ic.p2.wepayu.models.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servico para calculo da folha de pagamento, deducoes sindicais e emissao de relatorios.
 */
public class FolhaPagamentoService {
    private static final DateTimeFormatter FORMATO_DATA = new DateTimeFormatterBuilder()
            .parseStrict().appendPattern("d/M/uuuu").toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String LINHA = "=".repeat(127);
    private static final String NL = System.lineSeparator();

    public String totalFolha(EstadoSistema estado, String dataStr) throws Exception {
        LocalDate dia = parseData(dataStr);
        BigDecimal total = BigDecimal.ZERO;
        for (Empregado e : estado.getEmpregados().values()) {
            if (e.getAgenda().deveReceber(dia)) {
                total = total.add(e.calcularSalarioBruto(dia));
            }
        }
        return EmpregadoService.dinheiro(total);
    }

    public void rodaFolha(EstadoSistema estado, String dataStr, String saida) throws Exception {
        LocalDate dia = parseData(dataStr);
        String relatorio = estado.getFolhasGeradas().get(dia);
        if (relatorio == null) {
            List<LinhaPagamento> pagamentos = calcularFolha(estado, dia);
            relatorio = montarRelatorio(dia, pagamentos);
            efetivarPagamentos(pagamentos, dia);
            estado.getFolhasGeradas().put(dia, relatorio);
        }
        try {
            Files.writeString(Path.of(saida), relatorio, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ArquivoSaidaException();
        }
    }

    private List<LinhaPagamento> calcularFolha(EstadoSistema estado, LocalDate dia) {
        List<Empregado> ordenados = new ArrayList<>(estado.getEmpregados().values());
        ordenados.sort(Comparator.comparing(Empregado::getNome));
        List<LinhaPagamento> linhas = new ArrayList<>();
        for (Empregado e : ordenados) {
            if (e.getAgenda().deveReceber(dia)) {
                linhas.add(calcularLinha(e, dia));
            }
        }
        return linhas;
    }

    private LinhaPagamento calcularLinha(Empregado e, LocalDate dia) {
        LinhaPagamento linha = new LinhaPagamento(e);
        LocalDate inicioPeriodo = e.getAgenda().getInicioPeriodo(dia);
        LocalDate fimExclusivo = dia.plusDays(1);
        linha.bruto = e.calcularSalarioBruto(dia);

        if (e instanceof EmpregadoHorista horista) {
            linha.horasNormais = horista.getHorasNormais(inicioPeriodo, fimExclusivo);
            linha.horasExtras = horista.getHorasExtras(inicioPeriodo, fimExclusivo);
        } else if (e instanceof EmpregadoComissionado comissionado) {
            linha.fixo = comissionado.calcularSalarioFixo(dia);
            linha.vendas = comissionado.getVendasNoPeriodo(inicioPeriodo, fimExclusivo);
            linha.comissao = comissionado.calcularComissao(dia);
        }

        if (e.isSindicalizado() && linha.bruto.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate inicioDeducoes = e.getUltimoPagamento() == null
                    ? inicioInicialDeducoes(e, inicioPeriodo)
                    : e.getUltimoPagamento().plusDays(1);
            long dias = inicioDeducoes.isAfter(dia) ? 0 : ChronoUnit.DAYS.between(inicioDeducoes, dia) + 1;
            BigDecimal mensalidade = e.getSindicato().getTaxaSindical().multiply(BigDecimal.valueOf(dias));

            BigDecimal servicos = BigDecimal.ZERO;
            for (TaxaServico taxa : e.getSindicato().getTaxasServico()) {
                if (!taxa.getData().isBefore(inicioDeducoes) && taxa.getData().isBefore(fimExclusivo)) {
                    servicos = servicos.add(taxa.getValor());
                }
            }

            linha.deducoesCalculadas = centavos(e.getSindicato().getDebitoSindical().add(mensalidade).add(servicos));
            linha.descontos = linha.deducoesCalculadas.min(linha.bruto);
        }
        linha.liquido = centavos(linha.bruto.subtract(linha.descontos).max(BigDecimal.ZERO));
        return linha;
    }

    private LocalDate inicioInicialDeducoes(Empregado e, LocalDate inicioPeriodo) {
        if (e instanceof EmpregadoHorista horista && !horista.getCartoes().isEmpty()) {
            LocalDate primeiro = horista.getCartoes().stream()
                    .map(CartaoDePonto::getData)
                    .min(LocalDate::compareTo)
                    .orElse(inicioPeriodo);
            return primeiro.isAfter(inicioPeriodo) ? primeiro : inicioPeriodo;
        }
        return inicioPeriodo;
    }

    private void efetivarPagamentos(List<LinhaPagamento> pagamentos, LocalDate dia) {
        for (LinhaPagamento p : pagamentos) {
            if (p.bruto.compareTo(BigDecimal.ZERO) > 0) {
                p.empregado.setUltimoPagamento(dia);
                if (p.empregado.isSindicalizado()) {
                    p.empregado.getSindicato().setDebitoSindical(
                            p.deducoesCalculadas.subtract(p.descontos).max(BigDecimal.ZERO)
                    );
                }
            }
        }
    }

    private String montarRelatorio(LocalDate dia, List<LinhaPagamento> pagamentos) {
        String titulo = "FOLHA DE PAGAMENTO DO DIA " + dia;
        StringBuilder texto = new StringBuilder();
        texto.append(titulo).append(NL).append("=".repeat(titulo.length())).append(NL).append(NL);

        // HORISTAS
        texto.append(LINHA).append(NL)
                .append("===================== HORISTAS ================================================================================================").append(NL)
                .append(LINHA).append(NL)
                .append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo").append(NL)
                .append("==================================== ===== ===== ============= ========= =============== ======================================").append(NL);
        BigDecimal totalHoras = BigDecimal.ZERO, totalExtras = BigDecimal.ZERO;
        BigDecimal brutoH = BigDecimal.ZERO, descontosH = BigDecimal.ZERO, liquidoH = BigDecimal.ZERO;
        for (LinhaPagamento p : pagamentos) {
            if (!(p.empregado instanceof EmpregadoHorista)) continue;
            texto.append(String.format("%-36s %5s %5s %13s %9s %15s %s", p.empregado.getNome(),
                    EmpregadoService.decimalLivre(p.horasNormais), EmpregadoService.decimalLivre(p.horasExtras),
                    EmpregadoService.dinheiro(p.bruto), EmpregadoService.dinheiro(p.descontos),
                    EmpregadoService.dinheiro(p.liquido), p.empregado.getMetodoPagamento().getDescricao(p.empregado.getEndereco())))
                    .append(NL);
            totalHoras = totalHoras.add(p.horasNormais);
            totalExtras = totalExtras.add(p.horasExtras);
            brutoH = brutoH.add(p.bruto);
            descontosH = descontosH.add(p.descontos);
            liquidoH = liquidoH.add(p.liquido);
        }
        texto.append(NL).append(String.format("%-36s %5s %5s %13s %9s %15s", "TOTAL HORISTAS",
                EmpregadoService.decimalLivre(totalHoras), EmpregadoService.decimalLivre(totalExtras),
                EmpregadoService.dinheiro(brutoH), EmpregadoService.dinheiro(descontosH), EmpregadoService.dinheiro(liquidoH)))
                .append(NL).append(NL);

        // ASSALARIADOS
        texto.append(LINHA).append(NL)
                .append("===================== ASSALARIADOS ============================================================================================").append(NL)
                .append(LINHA).append(NL)
                .append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo").append(NL)
                .append("================================================ ============= ========= =============== ======================================").append(NL);
        BigDecimal brutoA = BigDecimal.ZERO, descontosA = BigDecimal.ZERO, liquidoA = BigDecimal.ZERO;
        for (LinhaPagamento p : pagamentos) {
            if (!(p.empregado instanceof EmpregadoAssalariado) || p.empregado instanceof EmpregadoComissionado) continue;
            texto.append(String.format("%-48s %13s %9s %15s %s", p.empregado.getNome(),
                    EmpregadoService.dinheiro(p.bruto), EmpregadoService.dinheiro(p.descontos),
                    EmpregadoService.dinheiro(p.liquido), p.empregado.getMetodoPagamento().getDescricao(p.empregado.getEndereco())))
                    .append(NL);
            brutoA = brutoA.add(p.bruto);
            descontosA = descontosA.add(p.descontos);
            liquidoA = liquidoA.add(p.liquido);
        }
        texto.append(NL).append(String.format("%-48s %13s %9s %15s", "TOTAL ASSALARIADOS",
                EmpregadoService.dinheiro(brutoA), EmpregadoService.dinheiro(descontosA), EmpregadoService.dinheiro(liquidoA)))
                .append(NL).append(NL);

        // COMISSIONADOS
        texto.append(LINHA).append(NL)
                .append("===================== COMISSIONADOS ===========================================================================================").append(NL)
                .append(LINHA).append(NL)
                .append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo").append(NL)
                .append("===================== ======== ======== ======== ============= ========= =============== ======================================").append(NL);
        BigDecimal fixo = BigDecimal.ZERO, vendas = BigDecimal.ZERO, comissao = BigDecimal.ZERO;
        BigDecimal brutoC = BigDecimal.ZERO, descontosC = BigDecimal.ZERO, liquidoC = BigDecimal.ZERO;
        for (LinhaPagamento p : pagamentos) {
            if (!(p.empregado instanceof EmpregadoComissionado)) continue;
            texto.append(String.format("%-21s %8s %8s %8s %13s %9s %15s %s", p.empregado.getNome(),
                    EmpregadoService.dinheiro(p.fixo), EmpregadoService.dinheiro(p.vendas), EmpregadoService.dinheiro(p.comissao),
                    EmpregadoService.dinheiro(p.bruto), EmpregadoService.dinheiro(p.descontos),
                    EmpregadoService.dinheiro(p.liquido), p.empregado.getMetodoPagamento().getDescricao(p.empregado.getEndereco())))
                    .append(NL);
            fixo = fixo.add(p.fixo);
            vendas = vendas.add(p.vendas);
            comissao = comissao.add(p.comissao);
            brutoC = brutoC.add(p.bruto);
            descontosC = descontosC.add(p.descontos);
            liquidoC = liquidoC.add(p.liquido);
        }
        texto.append(NL).append(String.format("%-21s %8s %8s %8s %13s %9s %15s", "TOTAL COMISSIONADOS",
                EmpregadoService.dinheiro(fixo), EmpregadoService.dinheiro(vendas), EmpregadoService.dinheiro(comissao),
                EmpregadoService.dinheiro(brutoC), EmpregadoService.dinheiro(descontosC), EmpregadoService.dinheiro(liquidoC)))
                .append(NL).append(NL);

        texto.append("TOTAL FOLHA: ").append(EmpregadoService.dinheiro(brutoH.add(brutoA).add(brutoC))).append(NL);
        return texto.toString();
    }

    private LocalDate parseData(String valor) throws DataInvalidaException {
        try {
            return LocalDate.parse(valor, FORMATO_DATA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new DataInvalidaException();
        }
    }

    private BigDecimal centavos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.DOWN);
    }

    private static class LinhaPagamento {
        private final Empregado empregado;
        private BigDecimal horasNormais = BigDecimal.ZERO;
        private BigDecimal horasExtras = BigDecimal.ZERO;
        private BigDecimal fixo = BigDecimal.ZERO;
        private BigDecimal vendas = BigDecimal.ZERO;
        private BigDecimal comissao = BigDecimal.ZERO;
        private BigDecimal bruto = BigDecimal.ZERO;
        private BigDecimal descontos = BigDecimal.ZERO;
        private BigDecimal deducoesCalculadas = BigDecimal.ZERO;
        private BigDecimal liquido = BigDecimal.ZERO;

        private LinhaPagamento(Empregado empregado) {
            this.empregado = empregado;
        }
    }
}
