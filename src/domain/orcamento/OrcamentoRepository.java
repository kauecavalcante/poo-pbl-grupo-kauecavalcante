package domain.orcamento;

import java.util.Optional;

public interface OrcamentoRepository {

    void salvar(Orcamento orcamento);

    Optional<Orcamento> buscarPorId(OrcamentoId id);
}
