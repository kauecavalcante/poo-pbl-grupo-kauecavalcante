package application.ordemservico;

import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import java.util.Objects;

public final class CancelarOrdemDeServico {

    private final OrdemDeServicoRepository osRepository;

    public CancelarOrdemDeServico(OrdemDeServicoRepository osRepository) {
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
    }

    public void executar(OrdemDeServicoId osId, String motivo) {
        OrdemDeServico os = osRepository.buscarPorId(osId)
            .orElseThrow(() -> new OrdemDeServicoNaoEncontrada(osId));
        os.cancelar(motivo);
        osRepository.salvar(os);
    }
}
