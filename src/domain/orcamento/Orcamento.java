package domain.orcamento;

import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Orcamento {

    private final OrcamentoId id;
    private final List<ItemDeOrcamento> itens;
    private EstadoOrcamento estado;

    private Orcamento(OrcamentoId id, List<ItemDeOrcamento> itens, EstadoOrcamento estado) {
        this.id = id;
        this.itens = itens;
        this.estado = estado;
    }

    public static Orcamento novo() {
        return new Orcamento(OrcamentoId.novo(), new ArrayList<>(), new Rascunho());
    }

    public static Orcamento reconstituir(OrcamentoId id, List<ItemDeOrcamento> itens) {
        if (id == null) {
            throw new IllegalArgumentException("id do orçamento não pode ser nulo");
        }
        if (itens == null) {
            throw new IllegalArgumentException("lista de itens não pode ser nula");
        }
        for (ItemDeOrcamento item : itens) {
            if (item == null) {
                throw new IllegalArgumentException("itens do orçamento não podem ser nulos");
            }
        }
        return new Orcamento(id, new ArrayList<>(itens), new Rascunho());
    }

    public OrcamentoId id() {
        return id;
    }

    public StatusOrcamento status() {
        return estado.status();
    }

    public Optional<String> motivoRejeicao() {
        return estado instanceof Rejeitado rejeitado ? Optional.of(rejeitado.motivo()) : Optional.empty();
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

    public void removerItem(ItemDeOrcamentoId itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("id do item de orçamento não pode ser nulo");
        }
        ItemDeOrcamento alvo = encontrar(itemId);
        itens.remove(alvo);
    }

    public void atualizarQuantidadeDeItem(ItemDeOrcamentoId itemId, int novaQuantidade) {
        if (itemId == null) {
            throw new IllegalArgumentException("id do item de orçamento não pode ser nulo");
        }
        encontrar(itemId).atualizarQuantidade(novaQuantidade);
    }

    private ItemDeOrcamento encontrar(ItemDeOrcamentoId itemId) {
        return itens.stream()
            .filter(i -> i.id().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("item não encontrado no orçamento"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Orcamento outro)) {
            return false;
        }
        return id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Orcamento{id=" + id
            + ", itens=" + itens.size()
            + ", total=" + total() + "}";
    }
}
