package application.ordemservico;

import application.excecao.OrdemDeServicoNaoEncontrada;
import application.excecao.PecaNaoEncontrada;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.OrcamentoRepository;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.peca.Peca;
import domain.peca.PecaRepository;
import java.util.List;
import java.util.Objects;

public final class MontarOrcamentoDaOS {

    private final OrdemDeServicoRepository osRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final PecaRepository pecaRepository;

    public MontarOrcamentoDaOS(OrdemDeServicoRepository osRepository,
                               OrcamentoRepository orcamentoRepository,
                               PecaRepository pecaRepository) {
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
        this.orcamentoRepository = Objects.requireNonNull(orcamentoRepository, "orcamentoRepository");
        this.pecaRepository = Objects.requireNonNull(pecaRepository, "pecaRepository");
    }

    public OrcamentoId executar(OrdemDeServicoId osId,
                                List<ItemDePecaInput> pecas,
                                List<ItemDeMaoDeObraInput> maosDeObra) {
        if (pecas == null || maosDeObra == null) {
            throw new IllegalArgumentException("listas de itens não podem ser nulas");
        }
        OrdemDeServico os = osRepository.buscarPorId(osId)
            .orElseThrow(() -> new OrdemDeServicoNaoEncontrada(osId));

        Orcamento orcamento = Orcamento.novo();
        for (ItemDePecaInput input : pecas) {
            // Preço e descrição vêm do catálogo (Peca), nunca do input — o
            // input só identifica qual peça e quantas unidades. Garante que
            // o orçamento reflete o preço vigente, não um valor injetado.
            Peca peca = pecaRepository.buscarPorId(input.pecaId())
                .orElseThrow(() -> new PecaNaoEncontrada(input.pecaId()));
            orcamento.adicionarItemDePeca(peca.id(), peca.descricao(), peca.preco(), input.quantidade());
        }
        for (ItemDeMaoDeObraInput input : maosDeObra) {
            orcamento.adicionarItemDeMaoDeObra(input.descricao(), input.precoUnitario(), input.quantidade());
        }
        // Envia o orçamento (transição RASCUNHO → ENVIADO) somente quando há
        // itens. Orçamento vazio fica em RASCUNHO; tentativas futuras de
        // aprová-lo falharão no domínio com mensagem clara.
        if (!orcamento.ehVazio()) {
            orcamento.enviar();
        }
        orcamentoRepository.salvar(orcamento);
        os.anexarOrcamento(orcamento.id());
        osRepository.salvar(os);
        return orcamento.id();
    }
}
