package domain.ordemservico;

import java.util.Optional;

public interface OrdemDeServicoRepository {

    void salvar(OrdemDeServico ordemDeServico);

    Optional<OrdemDeServico> buscarPorId(OrdemDeServicoId id);
}
