package application.ordemservico;

import application.excecao.OrcamentoNaoEncontrado;
import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.OrcamentoRepository;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import java.util.Objects;

public final class RejeitarOrcamentoDaOS {

    private final OrdemDeServicoRepository osRepository;
    private final OrcamentoRepository orcamentoRepository;

    public RejeitarOrcamentoDaOS(OrdemDeServicoRepository osRepository,
                                 OrcamentoRepository orcamentoRepository) {
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository, "orcamentoRepository");
    }

    public void executar(OrdemDeServicoId osId, String motivo) {
        OrdemDeServico os = osRepository.buscarPorId(osId)
            .orElseThrow(() -> new OrdemDeServicoNaoEncontrada(osId));
        OrcamentoId orcamentoId = os.orcamentoId()
            .orElseThrow(() -> new IllegalStateException("OS não possui orçamento anexado"));
        Orcamento orcamento = orcamentoRepository.buscarPorId(orcamentoId)
            .orElseThrow(() -> new OrcamentoNaoEncontrado(orcamentoId));
        orcamento.rejeitar(motivo);
        os.rejeitar(motivo);
        orcamentoRepository.salvar(orcamento);
        osRepository.salvar(os);
    }
}
