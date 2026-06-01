package application.excecao;

import domain.peca.PecaId;

public class PecaNaoEncontrada extends RuntimeException {

    public PecaNaoEncontrada(PecaId id) {
        super("peça não encontrada: " + id);
    }
}
