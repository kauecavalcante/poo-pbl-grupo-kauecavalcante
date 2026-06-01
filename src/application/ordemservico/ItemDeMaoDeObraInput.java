package application.ordemservico;

import domain.shared.Preco;

public record ItemDeMaoDeObraInput(String descricao, Preco precoUnitario, int quantidade) {
}
