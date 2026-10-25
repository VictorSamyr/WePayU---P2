package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.exceptions.DataFinalInvalidaException;
import br.ufal.ic.p2.wepayu.exceptions.DataInicialInvalidaException;
import br.ufal.ic.p2.wepayu.exceptions.DataInicialPosteriorException;
import br.ufal.ic.p2.wepayu.exceptions.DataInvalidaException;
import br.ufal.ic.p2.wepayu.exceptions.EmpregadoNaoEhSindicalizadoException;
import br.ufal.ic.p2.wepayu.exceptions.IdentificacaoMembroNulaException;
import br.ufal.ic.p2.wepayu.exceptions.IdentificacaoSindicatoJaExisteException;
import br.ufal.ic.p2.wepayu.exceptions.MembroNaoExisteException;
import br.ufal.ic.p2.wepayu.exceptions.ValorNumericoException;
import br.ufal.ic.p2.wepayu.exceptions.ValorPositivoException;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EstadoSistema;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Servico para operacoes e taxas relacionadas ao sindicato.
 */
public class SindicatoService {
    private static final DateTimeFormatter FORMATO_DATA = new DateTimeFormatterBuilder()
            .parseStrict().appendPattern("d/M/uuuu").toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    public void validarIdentificacaoUnica(EstadoSistema estado, String empId, String idSindicato)
            throws IdentificacaoSindicatoJaExisteException {
        for (Empregado outro : estado.getEmpregados().values()) {
            if (!outro.getId().equals(empId) && outro.isSindicalizado()
                    && idSindicato.equals(outro.getSindicato().getIdMembro())) {
                throw new IdentificacaoSindicatoJaExisteException();
            }
        }
    }

    public void lancaTaxaServico(EstadoSistema estado, String membro, String data, String valor) throws Exception {
        if (membro == null || membro.isEmpty()) {
            throw new IdentificacaoMembroNulaException();
        }
        Empregado encontrado = null;
        for (Empregado e : estado.getEmpregados().values()) {
            if (e.isSindicalizado() && membro.equals(e.getSindicato().getIdMembro())) {
                encontrado = e;
                break;
            }
        }
        if (encontrado == null) {
            throw new MembroNaoExisteException();
        }
        LocalDate dia = parseData(data, new DataInvalidaException());
        BigDecimal quantia = parseNumero(valor, new ValorNumericoException());
        if (quantia.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorPositivoException();
        }
        encontrado.getSindicato().adicionarTaxaServico(new TaxaServico(dia, quantia));
    }

    public String getTaxasServico(Empregado e, String dataInicial, String dataFinal) throws Exception {
        if (!e.isSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizadoException();
        }
        LocalDate[] datas = validarIntervalo(dataInicial, dataFinal);
        BigDecimal total = BigDecimal.ZERO;
        for (TaxaServico taxa : e.getSindicato().getTaxasServico()) {
            if (!taxa.getData().isBefore(datas[0]) && taxa.getData().isBefore(datas[1])) {
                total = total.add(taxa.getValor());
            }
        }
        return total.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }

    private LocalDate[] validarIntervalo(String inicio, String fim) throws Exception {
        LocalDate dataInicial = parseData(inicio, new DataInicialInvalidaException());
        LocalDate dataFinal = parseData(fim, new DataFinalInvalidaException());
        if (dataInicial.isAfter(dataFinal)) {
            throw new DataInicialPosteriorException();
        }
        return new LocalDate[]{dataInicial, dataFinal};
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
}
