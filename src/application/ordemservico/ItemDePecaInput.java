package application.ordemservico;

import domain.peca.PecaId;

public record ItemDePecaInput(PecaId pecaId, int quantidade) {
}
