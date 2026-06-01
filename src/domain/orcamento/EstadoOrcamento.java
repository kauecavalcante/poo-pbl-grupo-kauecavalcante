package domain.orcamento;

public interface EstadoOrcamento {

    StatusOrcamento status();

    boolean podeEditarItens();

    EstadoOrcamento aoEnviar(int quantidadeDeItens);

    EstadoOrcamento aoAprovar();

    EstadoOrcamento aoRejeitar(String motivo);
}
