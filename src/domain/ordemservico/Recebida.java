package domain.ordemservico;

public final class Recebida implements EstadoOS {

    @Override
    public StatusOS status() {
        return StatusOS.RECEBIDA;
    }

    @Override
    public boolean ehTerminal() {
        return false;
    }

    @Override
    public EstadoOS aoEnviarParaAprovacao() {
        return new AguardandoAprovacao();
    }

    @Override
    public EstadoOS aoAprovar() {
        throw new IllegalStateException("OS no estado RECEBIDA não pode ser aprovada");
    }

    @Override
    public EstadoOS aoRejeitar(String motivo) {
        throw new IllegalStateException("OS no estado RECEBIDA não pode ser rejeitada");
    }

    @Override
    public EstadoOS aoConcluir() {
        throw new IllegalStateException("OS no estado RECEBIDA não pode ser concluída");
    }

    @Override
    public EstadoOS aoEntregar() {
        throw new IllegalStateException("OS no estado RECEBIDA não pode ser entregue");
    }

    @Override
    public EstadoOS aoCancelar(String motivo) {
        return new Cancelada(motivo);
    }
}
