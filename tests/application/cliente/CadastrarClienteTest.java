package application.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.CpfJaCadastrado;
import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.shared.CPF;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CadastrarClienteTest {

    private static final CPF CPF_VALIDO = CPF.de("529.982.247-25");

    @Mock
    private ClienteRepository clienteRepository;

    @Test
    @DisplayName("cadastro feliz salva cliente e retorna id gerado")
    void cadastroFeliz() {
        when(clienteRepository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.empty());

        CadastrarCliente sut = new CadastrarCliente(clienteRepository);
        ClienteId id = sut.executar("Maria Silva", CPF_VALIDO, "11912345678");

        assertNotNull(id);
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).salvar(captor.capture());
        Cliente salvo = captor.getValue();
        assertEquals(id, salvo.id());
        assertEquals("Maria Silva", salvo.nome());
        assertEquals(CPF_VALIDO, salvo.cpf());
        assertEquals("11912345678", salvo.telefone());
    }

    @Test
    @DisplayName("CPF já cadastrado lança CpfJaCadastrado e não chama salvar")
    void cpfJaCadastrado() {
        Cliente existente = Cliente.novo("Outro nome", CPF_VALIDO, "11999998888");
        when(clienteRepository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.of(existente));

        CadastrarCliente sut = new CadastrarCliente(clienteRepository);
        CpfJaCadastrado ex = assertThrows(
            CpfJaCadastrado.class,
            () -> sut.executar("Maria Silva", CPF_VALIDO, "11912345678")
        );
        assertEquals("CPF já cadastrado: 529.982.247-25", ex.getMessage());
        verify(clienteRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("construtor rejeita repositório nulo")
    void construtorRejeitaRepoNulo() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CadastrarCliente(null)
        );
        assertEquals("clienteRepository", ex.getMessage());
    }

    @Test
    @DisplayName("nome inválido propaga IllegalArgumentException do domínio sem chamar salvar")
    void nomeInvalidoPropagaExcecaoDeDominio() {
        when(clienteRepository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.empty());

        CadastrarCliente sut = new CadastrarCliente(clienteRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar("", CPF_VALIDO, "11912345678")
        );
        verify(clienteRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("telefone inválido propaga IllegalArgumentException do domínio sem chamar salvar")
    void telefoneInvalidoPropagaExcecaoDeDominio() {
        when(clienteRepository.buscarPorCpf(CPF_VALIDO)).thenReturn(Optional.empty());

        CadastrarCliente sut = new CadastrarCliente(clienteRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar("Maria Silva", CPF_VALIDO, "123")
        );
        verify(clienteRepository, never()).salvar(any());
    }
}
