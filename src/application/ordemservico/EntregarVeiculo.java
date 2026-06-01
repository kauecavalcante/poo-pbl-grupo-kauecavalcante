package application.ordemservico;

import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import java.util.Objects;

public final class EntregarVeiculo {

    private final OrdemDeServicoRepository osRepository;

    public EntregarVeiculo(OrdemDeServicoRepository osRepository) {
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
    }

    public void executar(OrdemDeServicoId osId) {
        OrdemDeServico os = osRepository.buscarPorId(osId)
            .orElseThrow(() -> new OrdemDeServicoNaoEncontrada(osId));
        os.entregar();
        osRepository.salvar(os);
    }
}
