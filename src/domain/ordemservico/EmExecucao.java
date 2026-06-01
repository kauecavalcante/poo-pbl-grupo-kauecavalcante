package domain.ordemservico;

public final class EmExecucao implements EstadoOS {

    @Override
    public StatusOS status() {
        return StatusOS.EM_EXECUCAO;
    }

    @Override
    public boolean ehTerminal() {
        return false;
    }

    @Override
    public EstadoOS aoEnviarParaAprovacao() {
        throw new IllegalStateException("OS no estado EM_EXECUCAO não pode ser enviada para aprovação");
    }

    @Override
    public EstadoOS aoAprovar() {
        throw new IllegalStateException("OS no estado EM_EXECUCAO não pode ser aprovada");
    }

    @Override
    public EstadoOS aoRejeitar(String motivo) {
        throw new IllegalStateException("OS no estado EM_EXECUCAO não pode ser rejeitada");
    }

    @Override
    public EstadoOS aoConcluir() {
        return new Concluida();
    }

    @Override
    public EstadoOS aoEntregar() {
        throw new IllegalStateException("OS no estado EM_EXECUCAO não pode ser entregue");
    }

    @Override
    public EstadoOS aoCancelar(String motivo) {
        return new Cancelada(motivo);
    }
}
