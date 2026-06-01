package domain.ordemservico;

public final class Entregue implements EstadoOS {

    private static final String TERMINAL = "OS no estado ENTREGUE é estado terminal";

    @Override
    public StatusOS status() {
        return StatusOS.ENTREGUE;
    }

    @Override
    public boolean ehTerminal() {
        return true;
    }

    @Override
    public EstadoOS aoEnviarParaAprovacao() {
        throw new IllegalStateException(TERMINAL);
    }

    @Override
    public EstadoOS aoAprovar() {
        throw new IllegalStateException(TERMINAL);
    }

    @Override
    public EstadoOS aoRejeitar(String motivo) {
        throw new IllegalStateException(TERMINAL);
    }

    @Override
    public EstadoOS aoConcluir() {
        throw new IllegalStateException(TERMINAL);
    }

    @Override
    public EstadoOS aoEntregar() {
        throw new IllegalStateException(TERMINAL);
    }

    @Override
    public EstadoOS aoCancelar(String motivo) {
        throw new IllegalStateException(TERMINAL);
    }
}
