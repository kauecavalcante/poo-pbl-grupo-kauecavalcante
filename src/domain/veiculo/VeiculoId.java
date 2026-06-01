package domain.veiculo;

import java.util.Objects;
import java.util.UUID;

public final class VeiculoId {

    private final UUID valor;

    private VeiculoId(UUID valor) {
        this.valor = valor;
    }

    public static VeiculoId novo() {
        return new VeiculoId(UUID.randomUUID());
    }

    public static VeiculoId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do veículo não pode ser nulo");
        }
        return new VeiculoId(valor);
    }

    public static VeiculoId de(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id do veículo não pode ser nulo");
        }
        return new VeiculoId(UUID.fromString(valor));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VeiculoId outro)) {
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
