package domain.orcamento;

import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.ArrayList;
import java.util.List;

public final class Orcamento {

    private final OrcamentoId id;
    private final List<ItemDeOrcamento> itens;

    private Orcamento(OrcamentoId id, List<ItemDeOrcamento> itens) {
        this.id = id;
        this.itens = itens;
    }

    public static Orcamento novo() {
        return new Orcamento(OrcamentoId.novo(), new ArrayList<>());
    }

    public OrcamentoId id() {
        return id;
    }

    public int quantidadeDeItens() {
        return itens.size();
    }

    public boolean ehVazio() {
        return itens.isEmpty();
    }

    // Encapsulamento de agregado: a lista interna nunca vaza. List.copyOf
    // devolve um snapshot imutável — tentativas de mutar o retorno lançam
    // UnsupportedOperationException, e modificações futuras no agregado
    // não afetam cópias já devolvidas a chamadores anteriores.
    public List<ItemDeOrcamento> itens() {
        return List.copyOf(itens);
    }

    public Preco total() {
        return itens.stream()
            .map(ItemDeOrcamento::subtotal)
            .reduce(Preco.zero(), Preco::somar);
    }

    public ItemDeOrcamentoId adicionarItemDePeca(PecaId pecaId, String descricao,
                                                 Preco precoUnitario, int quantidade) {
        ItemDeOrcamento item = ItemDeOrcamento.dePeca(pecaId, descricao, precoUnitario, quantidade);
        itens.add(item);
        return item.id();
    }

    public ItemDeOrcamentoId adicionarItemDeMaoDeObra(String descricao,
                                                      Preco precoUnitario, int quantidade) {
        ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra(descricao, precoUnitario, quantidade);
        itens.add(item);
        return item.id();
    }
}
