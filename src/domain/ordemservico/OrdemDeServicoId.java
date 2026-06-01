package domain.ordemservico;

import java.util.Objects;
import java.util.UUID;

public final class OrdemDeServicoId {

    private final UUID valor;

    private OrdemDeServicoId(UUID valor) {
        this.valor = valor;
    }

    public static OrdemDeServicoId novo() {
        return new OrdemDeServicoId(UUID.randomUUID());
    }

    public static OrdemDeServicoId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id da ordem de serviço não pode ser nulo");
        }
        return new OrdemDeServicoId(valor);
    }

    public static OrdemDeServicoId de(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("id da ordem de serviço não pode ser nulo");
        }
        return new OrdemDeServicoId(UUID.fromString(valor));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrdemDeServicoId outro)) {
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
