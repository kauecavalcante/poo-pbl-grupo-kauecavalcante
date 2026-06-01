package domain.shared;

import java.math.BigDecimal;

public final class Preco {

    private final Dinheiro valor;

    private Preco(Dinheiro valor) {
        this.valor = valor;
    }

    public static Preco de(Dinheiro valor) {
        if (valor == null) {
            throw new IllegalArgumentException("preço não pode ser nulo");
        }
        if (valor.ehNegativo()) {
            throw new IllegalArgumentException("preço não pode ser negativo");
        }
        return new Preco(valor);
    }

    public static Preco deReais(BigDecimal valor) {
        return de(Dinheiro.deReais(valor));
    }

    public static Preco deReais(String valor) {
        return de(Dinheiro.deReais(valor));
    }

    public static Preco zero() {
        return de(Dinheiro.zeroReais());
    }

    public Dinheiro valor() {
        return valor;
    }
}
