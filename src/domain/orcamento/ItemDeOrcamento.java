package domain.orcamento;

import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.Objects;
import java.util.Optional;

public final class ItemDeOrcamento {

    private final ItemDeOrcamentoId id;
    private final TipoItem tipo;
    private final PecaId pecaId;
    private final String descricao;
    private final Preco precoUnitario;
    private int quantidade;

    private ItemDeOrcamento(ItemDeOrcamentoId id, TipoItem tipo, PecaId pecaId,
                            String descricao, Preco precoUnitario, int quantidade) {
        this.id = id;
        this.tipo = tipo;
        this.pecaId = pecaId;
        this.descricao = descricao;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public static ItemDeOrcamento dePeca(PecaId pecaId, String descricao, Preco precoUnitario, int quantidade) {
        if (pecaId == null) {
            throw new IllegalArgumentException("item de peça exige pecaId");
        }
        return new ItemDeOrcamento(
            ItemDeOrcamentoId.novo(),
            TipoItem.PECA,
            pecaId,
            validarDescricao(descricao),
            exigirPrecoNaoNulo(precoUnitario),
            validarQuantidade(quantidade)
        );
    }

    public static ItemDeOrcamento deMaoDeObra(String descricao, Preco precoUnitario, int quantidade) {
        return new ItemDeOrcamento(
            ItemDeOrcamentoId.novo(),
            TipoItem.MAO_DE_OBRA,
            null,
            validarDescricao(descricao),
            exigirPrecoNaoNulo(precoUnitario),
            validarQuantidade(quantidade)
        );
    }

    public static ItemDeOrcamento reconstituir(ItemDeOrcamentoId id, TipoItem tipo, PecaId pecaId,
                                               String descricao, Preco precoUnitario, int quantidade) {
        if (id == null) {
            throw new IllegalArgumentException("id do item de orçamento não pode ser nulo");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("tipo do item não pode ser nulo");
        }
        if (tipo == TipoItem.PECA && pecaId == null) {
            throw new IllegalArgumentException("item de peça exige pecaId");
        }
        if (tipo == TipoItem.MAO_DE_OBRA && pecaId != null) {
            throw new IllegalArgumentException("item de mão de obra não deve ter pecaId");
        }
        return new ItemDeOrcamento(
            id,
            tipo,
            pecaId,
            validarDescricao(descricao),
            exigirPrecoNaoNulo(precoUnitario),
            validarQuantidade(quantidade)
        );
    }

    public ItemDeOrcamentoId id() {
        return id;
    }

    public TipoItem tipo() {
        return tipo;
    }

    public Optional<PecaId> pecaId() {
        return Optional.ofNullable(pecaId);
    }

    public String descricao() {
        return descricao;
    }

    public Preco precoUnitario() {
        return precoUnitario;
    }

    public int quantidade() {
        return quantidade;
    }

    public boolean ehPeca() {
        return tipo == TipoItem.PECA;
    }

    public boolean ehMaoDeObra() {
        return tipo == TipoItem.MAO_DE_OBRA;
    }

    public Preco subtotal() {
        return precoUnitario.multiplicar(quantidade);
    }

    public void atualizarQuantidade(int novaQuantidade) {
        this.quantidade = validarQuantidade(novaQuantidade);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemDeOrcamento outro)) {
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
        return "ItemDeOrcamento{tipo=" + tipo
            + ", descricao=" + descricao
            + ", quantidade=" + quantidade
            + ", subtotal=" + subtotal() + "}";
    }

    private static String validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("descrição do item não pode ser vazia");
        }
        String normalizada = descricao.trim();
        if (normalizada.length() < 3) {
            throw new IllegalArgumentException("descrição do item deve ter ao menos 3 caracteres");
        }
        return normalizada;
    }

    private static Preco exigirPrecoNaoNulo(Preco preco) {
        if (preco == null) {
            throw new IllegalArgumentException("preço unitário não pode ser nulo");
        }
        return preco;
    }

    private static int validarQuantidade(int quantidade) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("quantidade do item deve ser positiva");
        }
        return quantidade;
    }
}
