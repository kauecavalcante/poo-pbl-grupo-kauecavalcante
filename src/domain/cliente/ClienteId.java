package domain.cliente;

import java.util.Objects;
import java.util.UUID;

public final class ClienteId {

    private final UUID valor;

    private ClienteId(UUID valor) {
        this.valor = valor;
    }

    public static ClienteId novo() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do cliente não pode ser nulo");
        }
        return new ClienteId(valor);
    }

    public static ClienteId de(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do cliente não pode ser nulo");
        }
        return new ClienteId(UUID.fromString(valor));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClienteId outro)) {
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
