package domain.ordemservico;

public interface EstadoOS {

    StatusOS status();

    boolean ehTerminal();

    EstadoOS aoEnviarParaAprovacao();

    EstadoOS aoAprovar();

    EstadoOS aoRejeitar(String motivo);

    EstadoOS aoConcluir();

    EstadoOS aoEntregar();

    EstadoOS aoCancelar(String motivo);
}
