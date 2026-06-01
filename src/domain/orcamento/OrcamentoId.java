package domain.orcamento;

import java.util.Objects;
import java.util.UUID;

public final class OrcamentoId {

    private final UUID valor;

    private OrcamentoId(UUID valor) {
        this.valor = valor;
    }

    public static OrcamentoId novo() {
        return new OrcamentoId(UUID.randomUUID());
    }

    public static OrcamentoId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do orçamento não pode ser nulo");
        }
        return new OrcamentoId(valor);
    }

    public static OrcamentoId de(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do orçamento não pode ser nulo");
        }
        return new OrcamentoId(UUID.fromString(valor));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrcamentoId outro)) {
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
