package domain.ordemservico;

public final class AguardandoAprovacao implements EstadoOS {

    @Override
    public StatusOS status() {
        return StatusOS.AGUARDANDO_APROVACAO;
    }

    @Override
    public boolean ehTerminal() {
        return false;
    }

    @Override
    public EstadoOS aoEnviarParaAprovacao() {
        throw new IllegalStateException("OS no estado AGUARDANDO_APROVACAO não pode ser reenviada para aprovação");
    }

    @Override
    public EstadoOS aoAprovar() {
        return new EmExecucao();
    }

    @Override
    public EstadoOS aoRejeitar(String motivo) {
        return new Rejeitada(motivo);
    }

    @Override
    public EstadoOS aoConcluir() {
        throw new IllegalStateException("OS no estado AGUARDANDO_APROVACAO não pode ser concluída");
    }

    @Override
    public EstadoOS aoEntregar() {
        throw new IllegalStateException("OS no estado AGUARDANDO_APROVACAO não pode ser entregue");
    }

    @Override
    public EstadoOS aoCancelar(String motivo) {
        return new Cancelada(motivo);
    }
}
