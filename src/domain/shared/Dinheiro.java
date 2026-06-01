package domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Dinheiro {

    private static final int ESCALA = 2;
    // HALF_EVEN (banker's rounding): elimina o viés positivo do HALF_UP em
    // sequências de arredondamentos, padrão para cálculo financeiro.
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;
    private static final Currency REAL = Currency.getInstance("BRL");

    private final BigDecimal valor;
    private final Currency moeda;

    private Dinheiro(BigDecimal valor, Currency moeda) {
        if (valor == null) {
            throw new IllegalArgumentException("valor não pode ser nulo");
        }
        if (moeda == null) {
            throw new IllegalArgumentException("moeda não pode ser nula");
        }
        this.valor = valor.setScale(ESCALA, ARREDONDAMENTO);
        this.moeda = moeda;
    }

    public static Dinheiro de(BigDecimal valor, Currency moeda) {
        return new Dinheiro(valor, moeda);
    }

    public static Dinheiro deReais(BigDecimal valor) {
        return new Dinheiro(valor, REAL);
    }

    public static Dinheiro deReais(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("valor não pode ser nulo");
        }
        return new Dinheiro(new BigDecimal(valor), REAL);
    }

    public static Dinheiro zeroReais() {
        return new Dinheiro(BigDecimal.ZERO, REAL);
    }

    public BigDecimal valor() {
        return valor;
    }

    public Currency moeda() {
        return moeda;
    }

    public Dinheiro somar(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        return new Dinheiro(this.valor.add(outro.valor), this.moeda);
    }

    public Dinheiro subtrair(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        return new Dinheiro(this.valor.subtract(outro.valor), this.moeda);
    }

    private void exigirMesmaMoeda(Dinheiro outro) {
        if (!this.moeda.equals(outro.moeda)) {
            throw new MoedasIncompativeisException(this.moeda, outro.moeda);
        }
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        return new Dinheiro(this.valor.multiply(fator), this.moeda);
    }

    public Dinheiro multiplicar(int fator) {
        return multiplicar(BigDecimal.valueOf(fator));
    }

    public boolean ehZero() {
        return valor.signum() == 0;
    }

    public boolean ehPositivo() {
        return valor.signum() > 0;
    }

    public boolean ehNegativo() {
        return valor.signum() < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dinheiro outro)) {
            return false;
        }
        return valor.equals(outro.valor) && moeda.equals(outro.moeda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, moeda);
    }
}
