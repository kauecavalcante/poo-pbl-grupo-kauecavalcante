package domain.ordemservico;

public final class Rejeitada implements EstadoOS {

    private static final String TERMINAL = "OS no estado REJEITADA é estado terminal";

    private final String motivo;

    public Rejeitada(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("motivo de rejeição não pode ser vazio");
        }
        String normalizado = motivo.trim();
        if (normalizado.length() < 3) {
            throw new IllegalArgumentException("motivo de rejeição deve ter ao menos 3 caracteres");
        }
        this.motivo = normalizado;
    }

    public String motivo() {
        return motivo;
    }

    @Override
    public StatusOS status() {
        return StatusOS.REJEITADA;
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
