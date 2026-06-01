package domain.orcamento;

public final class Rascunho implements EstadoOrcamento {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.RASCUNHO;
    }

    @Override
    public boolean podeEditarItens() {
        return true;
    }

    @Override
    public EstadoOrcamento aoEnviar(int quantidadeDeItens) {
        if (quantidadeDeItens == 0) {
            throw new IllegalStateException("orçamento sem itens não pode ser enviado");
        }
        return new Enviado();
    }

    @Override
    public EstadoOrcamento aoAprovar() {
        throw new IllegalStateException("orçamento em rascunho não pode ser aprovado");
    }

    @Override
    public EstadoOrcamento aoRejeitar(String motivo) {
        throw new IllegalStateException("orçamento em rascunho não pode ser rejeitado");
    }
}
