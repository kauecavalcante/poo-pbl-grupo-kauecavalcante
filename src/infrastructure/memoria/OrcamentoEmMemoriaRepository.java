package infrastructure.memoria;

import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.OrcamentoRepository;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class OrcamentoEmMemoriaRepository implements OrcamentoRepository {

    private final Map<OrcamentoId, Orcamento> dados = new ConcurrentHashMap<>();

    @Override
    public void salvar(Orcamento orcamento) {
        Objects.requireNonNull(orcamento, "orcamento");
        dados.put(orcamento.id(), orcamento);
    }

    @Override
    public Optional<Orcamento> buscarPorId(OrcamentoId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dados.get(id));
    }
}
