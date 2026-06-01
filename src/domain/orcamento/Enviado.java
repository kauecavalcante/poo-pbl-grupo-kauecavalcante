package domain.orcamento;

public final class Enviado implements EstadoOrcamento {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.ENVIADO;
    }

    @Override
    public boolean podeEditarItens() {
        return false;
    }

    @Override
    public EstadoOrcamento aoEnviar(int quantidadeDeItens) {
        throw new IllegalStateException("orçamento já enviado não pode ser reenviado");
    }

    @Override
    public EstadoOrcamento aoAprovar() {
        return new Aprovado();
    }

    @Override
    public EstadoOrcamento aoRejeitar(String motivo) {
        return new Rejeitado(motivo);
    }
}
