package application.excecao;

import domain.orcamento.OrcamentoId;

public class OrcamentoNaoEncontrado extends RuntimeException {

    public OrcamentoNaoEncontrado(OrcamentoId id) {
        super("orçamento não encontrado: " + id);
    }
}
