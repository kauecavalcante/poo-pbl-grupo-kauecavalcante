package domain.peca;

import domain.shared.Preco;
import java.util.Objects;

public final class Peca {

    // codigo é final: o catálogo identifica a peça pelo código; trocar código
    // equivale a registrar uma nova peça, não a alterar a existente.
    private final PecaId id;
    private final String codigo;
    private String descricao;
    private Preco preco;
    private int estoque;

    private Peca(PecaId id, String codigo, String descricao, Preco preco, int estoque) {
        this.id = id;
        this.codigo = codigo;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
    }

    public static Peca nova(String codigo, String descricao, Preco preco, int estoqueInicial) {
        exigirPrecoNaoNulo(preco);
        return new Peca(
            PecaId.novo(),
            validarCodigo(codigo),
            validarDescricao(descricao),
            preco,
            validarEstoque(estoqueInicial)
        );
    }

    public static Peca reconstituir(PecaId id, String codigo, String descricao, Preco preco, int estoque) {
        if (id == null) {
            throw new IllegalArgumentException("id da peça não pode ser nulo");
        }
        exigirPrecoNaoNulo(preco);
        return new Peca(
            id,
            validarCodigo(codigo),
            validarDescricao(descricao),
            preco,
            validarEstoque(estoque)
        );
    }

    public PecaId id() {
        return id;
    }

    public String codigo() {
        return codigo;
    }

    public String descricao() {
        return descricao;
    }

    public Preco preco() {
        return preco;
    }

    public int estoque() {
        return estoque;
    }

    public boolean temEstoque() {
        return estoque > 0;
    }

    public boolean temEstoqueSuficiente(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("quantidade não pode ser negativa");
        }
        return estoque >= quantidade;
    }

    public void alterarDescricao(String novaDescricao) {
        this.descricao = validarDescricao(novaDescricao);
    }

    public void alterarPreco(Preco novoPreco) {
        exigirPrecoNaoNulo(novoPreco);
        this.preco = novoPreco;
    }

    public void reduzirEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade a reduzir deve ser positiva");
        }
        if (quantidade > estoque) {
            throw new IllegalArgumentException("estoque insuficiente");
        }
        this.estoque -= quantidade;
    }

    public void reabastecerEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade a reabastecer deve ser positiva");
        }
        this.estoque += quantidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Peca outra)) {
            return false;
        }
        return id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String validarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("código da peça não pode ser vazio");
        }
        String normalizado = codigo.trim();
        if (normalizado.length() < 3) {
            throw new IllegalArgumentException("código da peça deve ter ao menos 3 caracteres");
        }
        if (normalizado.contains(" ")) {
            throw new IllegalArgumentException("código da peça não pode conter espaços");
        }
        return normalizado;
    }

    private static String validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("descrição da peça não pode ser vazia");
        }
        String normalizada = descricao.trim();
        if (normalizada.length() < 3) {
            throw new IllegalArgumentException("descrição da peça deve ter ao menos 3 caracteres");
        }
        return normalizada;
    }

    private static void exigirPrecoNaoNulo(Preco preco) {
        if (preco == null) {
            throw new IllegalArgumentException("preço não pode ser nulo");
        }
    }

    private static int validarEstoque(int estoque) {
        if (estoque < 0) {
            throw new IllegalArgumentException("estoque não pode ser negativo");
        }
        return estoque;
    }
}
