package application.veiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.ClienteNaoEncontrado;
import application.excecao.PlacaJaCadastrada;
import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
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
class CadastrarVeiculoTest {

    private static final Placa PLACA = Placa.de("ABC1234");
    private static final ClienteId DONO_ID = ClienteId.novo();

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Test
    @DisplayName("cadastro feliz salva veículo e retorna id gerado")
    void cadastroFeliz() {
        Cliente dono = Cliente.reconstituir(DONO_ID, "Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        when(clienteRepository.buscarPorId(DONO_ID)).thenReturn(Optional.of(dono));
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.empty());

        CadastrarVeiculo sut = new CadastrarVeiculo(veiculoRepository, clienteRepository);
        VeiculoId id = sut.executar(PLACA, "Fiat", "Uno", 2010, DONO_ID);

        assertNotNull(id);
        ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepository).salvar(captor.capture());
        Veiculo salvo = captor.getValue();
        assertEquals(id, salvo.id());
        assertEquals(PLACA, salvo.placa());
        assertEquals("Fiat", salvo.marca());
        assertEquals("Uno", salvo.modelo());
        assertEquals(2010, salvo.ano());
        assertEquals(DONO_ID, salvo.dono());
    }

    @Test
    @DisplayName("cliente inexistente lança ClienteNaoEncontrado e não chama salvar")
    void clienteInexistente() {
        when(clienteRepository.buscarPorId(DONO_ID)).thenReturn(Optional.empty());

        CadastrarVeiculo sut = new CadastrarVeiculo(veiculoRepository, clienteRepository);
        ClienteNaoEncontrado ex = assertThrows(
            ClienteNaoEncontrado.class,
            () -> sut.executar(PLACA, "Fiat", "Uno", 2010, DONO_ID)
        );
        assertEquals("cliente não encontrado: " + DONO_ID, ex.getMessage());
        verify(veiculoRepository, never()).salvar(any());
        verify(veiculoRepository, never()).buscarPorPlaca(any());
    }

    @Test
    @DisplayName("placa já cadastrada lança PlacaJaCadastrada e não chama salvar")
    void placaJaCadastrada() {
        Cliente dono = Cliente.reconstituir(DONO_ID, "Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        Veiculo existente = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO_ID);
        when(clienteRepository.buscarPorId(DONO_ID)).thenReturn(Optional.of(dono));
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.of(existente));

        CadastrarVeiculo sut = new CadastrarVeiculo(veiculoRepository, clienteRepository);
        PlacaJaCadastrada ex = assertThrows(
            PlacaJaCadastrada.class,
            () -> sut.executar(PLACA, "Fiat", "Uno", 2010, DONO_ID)
        );
        assertEquals("placa já cadastrada: ABC-1234", ex.getMessage());
        verify(veiculoRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("construtor rejeita veiculoRepository nulo")
    void construtorRejeitaVeiculoRepoNulo() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CadastrarVeiculo(null, clienteRepository)
        );
        assertEquals("veiculoRepository", ex.getMessage());
    }

    @Test
    @DisplayName("construtor rejeita clienteRepository nulo")
    void construtorRejeitaClienteRepoNulo() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CadastrarVeiculo(veiculoRepository, null)
        );
        assertEquals("clienteRepository", ex.getMessage());
    }

    @Test
    @DisplayName("placa nula propaga IllegalArgumentException do domínio sem chamar salvar")
    void placaNulaPropagaExcecaoDeDominio() {
        Cliente dono = Cliente.reconstituir(DONO_ID, "Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        when(clienteRepository.buscarPorId(DONO_ID)).thenReturn(Optional.of(dono));

        CadastrarVeiculo sut = new CadastrarVeiculo(veiculoRepository, clienteRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(null, "Fiat", "Uno", 2010, DONO_ID)
        );
        verify(veiculoRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("dono nulo propaga IllegalArgumentException do domínio sem consultar repositórios")
    void donoNuloPropagaExcecaoDeDominio() {
        CadastrarVeiculo sut = new CadastrarVeiculo(veiculoRepository, clienteRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(PLACA, "Fiat", "Uno", 2010, null)
        );
        verify(veiculoRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("ano fora do intervalo propaga IllegalArgumentException do domínio")
    void anoForaDoIntervaloPropagaExcecaoDeDominio() {
        Cliente dono = Cliente.reconstituir(DONO_ID, "Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        when(clienteRepository.buscarPorId(DONO_ID)).thenReturn(Optional.of(dono));
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.empty());

        CadastrarVeiculo sut = new CadastrarVeiculo(veiculoRepository, clienteRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(PLACA, "Fiat", "Uno", 1800, DONO_ID)
        );
        verify(veiculoRepository, never()).salvar(any());
    }
}
