package domain.orcamento;

import domain.peca.PecaId;
import domain.shared.Preco;
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
