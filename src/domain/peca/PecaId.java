package domain.peca;

import java.util.Objects;
import java.util.UUID;

public final class PecaId {

    private final UUID valor;

    private PecaId(UUID valor) {
        this.valor = valor;
    }

    public static PecaId novo() {
        return new PecaId(UUID.randomUUID());
    }

    public static PecaId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id da peça não pode ser nulo");
        }
        return new PecaId(valor);
    }

    public static PecaId de(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id da peça não pode ser nulo");
        }
        return new PecaId(UUID.fromString(valor));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PecaId outro)) {
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
