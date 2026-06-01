package application.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.ClienteNaoEncontrado;
import application.excecao.VeiculoNaoEncontrado;
import application.excecao.VeiculoNaoPertenceAoCliente;
import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.shared.CPF;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import domain.veiculo.VeiculoRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbrirOrdemDeServicoTest {

    private static final ClienteId CLIENTE_ID = ClienteId.novo();
    private static final VeiculoId VEICULO_ID = VeiculoId.novo();
    private static final Cliente CLIENTE = Cliente.reconstituir(
        CLIENTE_ID, "Maria Silva", CPF.de("529.982.247-25"), "11912345678"
    );
    private static final Veiculo VEICULO = Veiculo.reconstituir(
        VEICULO_ID, Placa.de("ABC1234"), "Fiat", "Uno", 2010, CLIENTE_ID
    );

    @Mock private OrdemDeServicoRepository osRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private VeiculoRepository veiculoRepository;

    @Test
    @DisplayName("abertura feliz salva OS e retorna id")
    void aberturaFeliz() {
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
        when(veiculoRepository.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(VEICULO));

        AbrirOrdemDeServico sut = new AbrirOrdemDeServico(osRepository, clienteRepository, veiculoRepository);
        OrdemDeServicoId id = sut.executar(CLIENTE_ID, VEICULO_ID);

        assertNotNull(id);
        ArgumentCaptor<OrdemDeServico> captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        verify(osRepository).salvar(captor.capture());
        OrdemDeServico salva = captor.getValue();
        assertEquals(id, salva.id());
        assertEquals(CLIENTE_ID, salva.clienteId());
        assertEquals(VEICULO_ID, salva.veiculoId());
    }

    @Test
    @DisplayName("cliente inexistente lança ClienteNaoEncontrado antes de consultar veículo")
    void clienteInexistente() {
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());

        AbrirOrdemDeServico sut = new AbrirOrdemDeServico(osRepository, clienteRepository, veiculoRepository);
        assertThrows(
            ClienteNaoEncontrado.class,
            () -> sut.executar(CLIENTE_ID, VEICULO_ID)
        );
        verify(veiculoRepository, never()).buscarPorId(any());
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("veículo inexistente lança VeiculoNaoEncontrado")
    void veiculoInexistente() {
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
        when(veiculoRepository.buscarPorId(VEICULO_ID)).thenReturn(Optional.empty());

        AbrirOrdemDeServico sut = new AbrirOrdemDeServico(osRepository, clienteRepository, veiculoRepository);
        assertThrows(
            VeiculoNaoEncontrado.class,
            () -> sut.executar(CLIENTE_ID, VEICULO_ID)
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("veículo pertence a outro dono lança VeiculoNaoPertenceAoCliente")
    void veiculoDeOutroDono() {
        ClienteId outroDono = ClienteId.novo();
        Veiculo veiculoDeOutro = Veiculo.reconstituir(
            VEICULO_ID, Placa.de("XYZ4321"), "Fiat", "Uno", 2010, outroDono
        );
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));
        when(veiculoRepository.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoDeOutro));

        AbrirOrdemDeServico sut = new AbrirOrdemDeServico(osRepository, clienteRepository, veiculoRepository);
        VeiculoNaoPertenceAoCliente ex = assertThrows(
            VeiculoNaoPertenceAoCliente.class,
            () -> sut.executar(CLIENTE_ID, VEICULO_ID)
        );
        assertEquals("veículo " + VEICULO_ID + " não pertence ao cliente " + CLIENTE_ID, ex.getMessage());
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("clienteId nulo propaga IllegalArgumentException do domínio")
    void clienteIdNuloPropagaExcecaoDeDominio() {
        when(veiculoRepository.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(VEICULO));

        AbrirOrdemDeServico sut = new AbrirOrdemDeServico(osRepository, clienteRepository, veiculoRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(null, VEICULO_ID)
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("veiculoId nulo propaga IllegalArgumentException do domínio")
    void veiculoIdNuloPropagaExcecaoDeDominio() {
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(CLIENTE));

        AbrirOrdemDeServico sut = new AbrirOrdemDeServico(osRepository, clienteRepository, veiculoRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(CLIENTE_ID, null)
        );
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("construtor rejeita osRepository nulo")
    void rejeitaOsRepoNulo() {
        assertThrows(
            NullPointerException.class,
            () -> new AbrirOrdemDeServico(null, clienteRepository, veiculoRepository)
        );
    }

    @Test
    @DisplayName("construtor rejeita clienteRepository nulo")
    void rejeitaClienteRepoNulo() {
        assertThrows(
            NullPointerException.class,
            () -> new AbrirOrdemDeServico(osRepository, null, veiculoRepository)
        );
    }

    @Test
    @DisplayName("construtor rejeita veiculoRepository nulo")
    void rejeitaVeiculoRepoNulo() {
        assertThrows(
            NullPointerException.class,
            () -> new AbrirOrdemDeServico(osRepository, clienteRepository, null)
        );
    }
}
