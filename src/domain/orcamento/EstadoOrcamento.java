package domain.orcamento;

// sealed + permits: fixa o conjunto fechado de estados possíveis. Tentar
// introduzir um quinto estado a partir de fora do pacote falha em tempo de
// compilação, e pattern matching exaustivo (switch sobre EstadoOrcamento)
// fica garantido pelo compilador.
public sealed interface EstadoOrcamento
    permits Rascunho, Enviado, Aprovado, Rejeitado {

    StatusOrcamento status();

    boolean podeEditarItens();

    EstadoOrcamento aoEnviar(int quantidadeDeItens);

    EstadoOrcamento aoAprovar();

    EstadoOrcamento aoRejeitar(String motivo);
}
