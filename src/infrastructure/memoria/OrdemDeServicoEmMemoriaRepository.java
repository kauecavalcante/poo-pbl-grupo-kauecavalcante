package infrastructure.memoria;

import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class OrdemDeServicoEmMemoriaRepository implements OrdemDeServicoRepository {

    private final Map<OrdemDeServicoId, OrdemDeServico> dados = new ConcurrentHashMap<>();

    @Override
    public void salvar(OrdemDeServico ordemDeServico) {
        Objects.requireNonNull(ordemDeServico, "ordemDeServico");
        dados.put(ordemDeServico.id(), ordemDeServico);
    }

    @Override
    public Optional<OrdemDeServico> buscarPorId(OrdemDeServicoId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dados.get(id));
    }
}
