package infrastructure.memoria;

import domain.peca.Peca;
import domain.peca.PecaId;
import domain.peca.PecaRepository;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PecaEmMemoriaRepository implements PecaRepository {

    private final Map<PecaId, Peca> dados = new ConcurrentHashMap<>();

    @Override
    public void salvar(Peca peca) {
        Objects.requireNonNull(peca, "peca");
        dados.put(peca.id(), peca);
    }

    @Override
    public Optional<Peca> buscarPorId(PecaId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Peca> buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return dados.values().stream()
            .filter(p -> p.codigo().equals(codigo))
            .findFirst();
    }
}
