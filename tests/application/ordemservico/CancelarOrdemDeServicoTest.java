package application.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.cliente.ClienteId;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.ordemservico.StatusOS;
import domain.veiculo.VeiculoId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelarOrdemDeServicoTest {

    @Mock private OrdemDeServicoRepository osRepository;

    @Test
    @DisplayName("cancelamento feliz transita para CANCELADA e persiste")
    void cancelamentoFeliz() {
        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));

        CancelarOrdemDeServico sut = new CancelarOrdemDeServico(osRepository);
        sut.executar(os.id(), "Cliente desistiu do serviço");

        assertEquals(StatusOS.CANCELADA, os.status());
        assertEquals(Optional.of("Cliente desistiu do serviço"), os.motivoCancelamento());
        verify(osRepository).salvar(os);
    }

    @Test
    @DisplayName("OS não encontrada lança OrdemDeServicoNaoEncontrada sem salvar")
    void osNaoEncontrada() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        when(osRepository.buscarPorId(id)).thenReturn(Optional.empty());

        CancelarOrdemDeServico sut = new CancelarOrdemDeServico(osRepository);
        assertThrows(
            OrdemDeServicoNaoEncontrada.class,
            () -> sut.executar(id, "Cliente desistiu")
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("OS já em estado terminal propaga IllegalStateException do domínio")
    void osEmEstadoTerminal() {
        OrdemDeServico osCancelada = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        osCancelada.cancelar("Primeiro cancelamento");
        when(osRepository.buscarPorId(osCancelada.id())).thenReturn(Optional.of(osCancelada));

        CancelarOrdemDeServico sut = new CancelarOrdemDeServico(osRepository);
        assertThrows(
            IllegalStateException.class,
            () -> sut.executar(osCancelada.id(), "Segundo cancelamento")
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("motivo inválido propaga IllegalArgumentException do domínio")
    void motivoInvalido() {
        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));

        CancelarOrdemDeServico sut = new CancelarOrdemDeServico(osRepository);
        assertThrows(IllegalArgumentException.class, () -> sut.executar(os.id(), "AB"));
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("construtor rejeita repositório nulo")
    void rejeitaRepoNulo() {
        assertThrows(NullPointerException.class, () -> new CancelarOrdemDeServico(null));
    }
}
