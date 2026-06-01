package domain.orcamento;

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

    public Preco total() {
        return itens.stream()
            .map(ItemDeOrcamento::subtotal)
            .reduce(Preco.zero(), Preco::somar);
    }
}
