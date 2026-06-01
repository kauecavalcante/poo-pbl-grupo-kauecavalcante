package domain.peca;

import java.util.Optional;

public interface PecaRepository {

    void salvar(Peca peca);

    Optional<Peca> buscarPorId(PecaId id);

    Optional<Peca> buscarPorCodigo(String codigo);
}
