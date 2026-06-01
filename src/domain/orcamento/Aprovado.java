package domain.orcamento;

public final class Aprovado implements EstadoOrcamento {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.APROVADO;
    }

    @Override
    public boolean podeEditarItens() {
        return false;
    }

    @Override
    public EstadoOrcamento aoEnviar(int quantidadeDeItens) {
        throw new IllegalStateException("orçamento aprovado é estado terminal");
    }

    @Override
    public EstadoOrcamento aoAprovar() {
        throw new IllegalStateException("orçamento aprovado é estado terminal");
    }

    @Override
    public EstadoOrcamento aoRejeitar(String motivo) {
        throw new IllegalStateException("orçamento aprovado é estado terminal");
    }
}
