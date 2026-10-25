package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.EstadoSistema;
import br.ufal.ic.p2.wepayu.persistence.PersistenciaService;
import br.ufal.ic.p2.wepayu.services.*;

import java.nio.file.Path;

/**
 * Fachada do sistema WePayU usada pelos testes de aceitacao.
 * Apenas encaminha chamadas para as classes de servico e gerencia transacoes.
 */
public class Facade {
    private static final Path ARQUIVO_DADOS = Path.of("wepayu.dat");

    private EstadoSistema estado;
    private final PersistenciaService persistenciaService = new PersistenciaService();
    private final HistoricoService historicoService = new HistoricoService();
    private final AgendaService agendaService = new AgendaService();
    private final SindicatoService sindicatoService = new SindicatoService();
    private final EmpregadoService empregadoService = new EmpregadoService(agendaService, sindicatoService);
    private final FolhaPagamentoService folhaService = new FolhaPagamentoService();

    public Facade() {
        estado = persistenciaService.carregar(ARQUIVO_DADOS);
    }

    public void zerarSistema() throws Exception {
        historicoService.verificarAtivo();
        historicoService.registrarAlteracao(estado);
        estado = new EstadoSistema();
    }

    public void encerrarSistema() throws Exception {
        historicoService.verificarAtivo();
        persistenciaService.salvar(ARQUIVO_DADOS, estado);
        historicoService.encerrar();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        String id = empregadoService.criarEmpregado(copia, nome, endereco, tipo, salario);
        historicoService.registrarAlteracao(estado);
        estado = copia;
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo,
                                 String salario, String comissao) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        String id = empregadoService.criarEmpregado(copia, nome, endereco, tipo, salario, comissao);
        historicoService.registrarAlteracao(estado);
        estado = copia;
        return id;
    }

    public void removerEmpregado(String emp) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.removerEmpregado(copia, emp);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public int getNumeroDeEmpregados() throws Exception {
        historicoService.verificarAtivo();
        return empregadoService.getNumeroDeEmpregados(estado);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        historicoService.verificarAtivo();
        return empregadoService.getEmpregadoPorNome(estado, nome, indice);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        historicoService.verificarAtivo();
        return empregadoService.getAtributoEmpregado(estado, emp, atributo);
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.alteraEmpregado(copia, emp, atributo, valor);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String adicional) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.alteraEmpregado(copia, emp, atributo, valor, adicional);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public void alteraEmpregado(String emp, String atributo, String valor,
                                String idSindicato, String taxaSindical) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.alteraEmpregado(copia, emp, atributo, valor, idSindicato, taxaSindical);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public void alteraEmpregado(String emp, String atributo, String valor,
                                String banco, String agencia, String contaCorrente) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.alteraEmpregado(copia, emp, atributo, valor, banco, agencia, contaCorrente);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.lancaCartao(copia, emp, data, horas);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        historicoService.verificarAtivo();
        return empregadoService.getHorasNormaisTrabalhadas(estado, emp, dataInicial, dataFinal);
    }

    public String getHorasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        historicoService.verificarAtivo();
        return empregadoService.getHorasExtrasTrabalhadas(estado, emp, dataInicial, dataFinal);
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        empregadoService.lancaVenda(copia, emp, data, valor);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        historicoService.verificarAtivo();
        return empregadoService.getVendasRealizadas(estado, emp, dataInicial, dataFinal);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        sindicatoService.lancaTaxaServico(copia, membro, data, valor);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        historicoService.verificarAtivo();
        br.ufal.ic.p2.wepayu.models.Empregado e = empregadoService.buscarEmpregado(estado, emp);
        return sindicatoService.getTaxasServico(e, dataInicial, dataFinal);
    }

    public String totalFolha(String data) throws Exception {
        historicoService.verificarAtivo();
        return folhaService.totalFolha(estado, data);
    }

    public void rodaFolha(String data, String saida) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        folhaService.rodaFolha(copia, data, saida);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        historicoService.verificarAtivo();
        EstadoSistema copia = estado.copiar();
        agendaService.criarAgendaDePagamentos(copia, descricao);
        historicoService.registrarAlteracao(estado);
        estado = copia;
    }

    public void undo() throws Exception {
        historicoService.verificarAtivo();
        estado = historicoService.desfazer(estado);
    }

    public void redo() throws Exception {
        historicoService.verificarAtivo();
        estado = historicoService.refazer(estado);
    }
}
