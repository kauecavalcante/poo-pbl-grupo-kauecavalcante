package domain.shared;

import java.math.BigDecimal;
import java.util.Objects;

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

    public Preco somar(Preco outro) {
        return de(this.valor.somar(outro.valor));
    }

    // Preco impõe restrição mais estrita que Dinheiro: multiplicar por fator
    // negativo violaria a invariante de não-negatividade construtora desta classe.
    public Preco multiplicar(int fator) {
        if (fator < 0) {
            throw new IllegalArgumentException("fator de multiplicação não pode ser negativo");
        }
        return de(this.valor.multiplicar(fator));
    }

    public Preco multiplicar(BigDecimal fator) {
        if (fator.signum() < 0) {
            throw new IllegalArgumentException("fator de multiplicação não pode ser negativo");
        }
        return de(this.valor.multiplicar(fator));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Preco outro)) {
            return false;
        }
        return valor.equals(outro.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
