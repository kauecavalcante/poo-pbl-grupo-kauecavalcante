package domain.orcamento;

import java.util.Objects;
import java.util.UUID;

public final class ItemDeOrcamentoId {

    private final UUID valor;

    private ItemDeOrcamentoId(UUID valor) {
        this.valor = valor;
    }

    public static ItemDeOrcamentoId novo() {
        return new ItemDeOrcamentoId(UUID.randomUUID());
    }

    public static ItemDeOrcamentoId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do item de orçamento não pode ser nulo");
        }
        return new ItemDeOrcamentoId(valor);
    }

    public static ItemDeOrcamentoId de(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do item de orçamento não pode ser nulo");
        }
        return new ItemDeOrcamentoId(UUID.fromString(valor));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemDeOrcamentoId outro)) {
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
