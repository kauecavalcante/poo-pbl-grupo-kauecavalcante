package domain.ordemservico;

public final class Concluida implements EstadoOS {

    @Override
    public StatusOS status() {
        return StatusOS.CONCLUIDA;
    }

    @Override
    public boolean ehTerminal() {
        return false;
    }

    @Override
    public EstadoOS aoEnviarParaAprovacao() {
        throw new IllegalStateException("OS no estado CONCLUIDA não pode ser enviada para aprovação");
    }

    @Override
    public EstadoOS aoAprovar() {
        throw new IllegalStateException("OS no estado CONCLUIDA não pode ser aprovada");
    }

    @Override
    public EstadoOS aoRejeitar(String motivo) {
        throw new IllegalStateException("OS no estado CONCLUIDA não pode ser rejeitada");
    }

    @Override
    public EstadoOS aoConcluir() {
        throw new IllegalStateException("OS no estado CONCLUIDA não pode ser concluída novamente");
    }

    @Override
    public EstadoOS aoEntregar() {
        return new Entregue();
    }

    @Override
    public EstadoOS aoCancelar(String motivo) {
        return new Cancelada(motivo);
    }
}
