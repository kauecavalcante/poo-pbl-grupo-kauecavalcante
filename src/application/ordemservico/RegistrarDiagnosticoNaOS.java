package application.ordemservico;

import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import java.util.Objects;

public final class RegistrarDiagnosticoNaOS {

    private final OrdemDeServicoRepository osRepository;

    public RegistrarDiagnosticoNaOS(OrdemDeServicoRepository osRepository) {
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
    }

    public void executar(OrdemDeServicoId osId, String diagnostico) {
        OrdemDeServico os = osRepository.buscarPorId(osId)
            .orElseThrow(() -> new OrdemDeServicoNaoEncontrada(osId));
        os.registrarDiagnostico(diagnostico);
        osRepository.salvar(os);
    }
}
