package domain.ordemservico;

// sealed + permits: fixa o conjunto fechado dos 7 estados da OS. Pattern
// matching exaustivo passa a ser verificado pelo compilador, e nenhum
// estado externo pode contaminar a máquina de estados.
public sealed interface EstadoOS
    permits Recebida, AguardandoAprovacao, EmExecucao, Concluida, Entregue, Rejeitada, Cancelada {

    StatusOS status();

    boolean ehTerminal();

    EstadoOS aoEnviarParaAprovacao();

    EstadoOS aoAprovar();

    EstadoOS aoRejeitar(String motivo);

    EstadoOS aoConcluir();

    EstadoOS aoEntregar();

    EstadoOS aoCancelar(String motivo);
}
