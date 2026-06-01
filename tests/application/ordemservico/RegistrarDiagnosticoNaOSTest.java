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
import domain.veiculo.VeiculoId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarDiagnosticoNaOSTest {

    @Mock private OrdemDeServicoRepository osRepository;

    private final OrdemDeServico OS = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());

    @Test
    @DisplayName("registro feliz delega ao agregado e persiste a OS")
    void registroFeliz() {
        when(osRepository.buscarPorId(OS.id())).thenReturn(Optional.of(OS));

        RegistrarDiagnosticoNaOS sut = new RegistrarDiagnosticoNaOS(osRepository);
        sut.executar(OS.id(), "Suspensão dianteira com folga");

        assertEquals(Optional.of("Suspensão dianteira com folga"), OS.diagnostico());
        verify(osRepository).salvar(OS);
    }

    @Test
    @DisplayName("OS não encontrada lança OrdemDeServicoNaoEncontrada sem chamar salvar")
    void osNaoEncontrada() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        when(osRepository.buscarPorId(id)).thenReturn(Optional.empty());

        RegistrarDiagnosticoNaOS sut = new RegistrarDiagnosticoNaOS(osRepository);
        assertThrows(
            OrdemDeServicoNaoEncontrada.class,
            () -> sut.executar(id, "Suspensão dianteira com folga")
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("diagnóstico inválido propaga IllegalArgumentException do domínio sem chamar salvar")
    void diagnosticoInvalido() {
        when(osRepository.buscarPorId(OS.id())).thenReturn(Optional.of(OS));

        RegistrarDiagnosticoNaOS sut = new RegistrarDiagnosticoNaOS(osRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(OS.id(), "AB")
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("estado errado propaga IllegalStateException do domínio sem chamar salvar")
    void estadoErradoPropagaExcecaoDeDominio() {
        OrdemDeServico osCancelada = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        osCancelada.cancelar("Cliente desistiu");
        when(osRepository.buscarPorId(osCancelada.id())).thenReturn(Optional.of(osCancelada));

        RegistrarDiagnosticoNaOS sut = new RegistrarDiagnosticoNaOS(osRepository);
        assertThrows(
            IllegalStateException.class,
            () -> sut.executar(osCancelada.id(), "Suspensão com folga")
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("construtor rejeita repositório nulo")
    void construtorRejeitaRepoNulo() {
        assertThrows(NullPointerException.class, () -> new RegistrarDiagnosticoNaOS(null));
    }
}
