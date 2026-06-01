package infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.shared.CPF;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteEmMemoriaRepositoryTest {

    private static final CPF CPF_VALIDO = CPF.de("529.982.247-25");

    @Test
    @DisplayName("salvar e buscarPorId devolve a entidade exata salva")
    void salvarEBuscar() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        Cliente cliente = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
        repo.salvar(cliente);
        assertSame(cliente, repo.buscarPorId(cliente.id()).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorId com ID inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(ClienteId.novo()));
    }

    @Test
    @DisplayName("buscarPorId com null retorna Optional.empty")
    void buscarPorIdNulo() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("salvar com mesmo ID substitui a versão anterior")
    void atualizar() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        ClienteId id = ClienteId.novo();
        Cliente antes = Cliente.reconstituir(id, "Maria Silva", CPF_VALIDO, "11912345678");
        Cliente depois = Cliente.reconstituir(id, "Maria S. Pereira", CPF_VALIDO, "11999998888");
        repo.salvar(antes);
        repo.salvar(depois);
        assertSame(depois, repo.buscarPorId(id).orElseThrow());
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException com mensagem cliente")
    void salvarNulo() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("cliente", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorCpf encontra cliente cadastrado")
    void buscarPorCpfFeliz() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        Cliente cliente = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
        repo.salvar(cliente);
        assertSame(cliente, repo.buscarPorCpf(CPF_VALIDO).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorCpf com CPF não cadastrado retorna Optional.empty")
    void buscarPorCpfInexistente() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        repo.salvar(Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678"));
        assertEquals(Optional.empty(), repo.buscarPorCpf(CPF.de("111.444.777-35")));
    }

    @Test
    @DisplayName("buscarPorCpf com null retorna Optional.empty")
    void buscarPorCpfNulo() {
        ClienteEmMemoriaRepository repo = new ClienteEmMemoriaRepository();
        repo.salvar(Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678"));
        assertTrue(repo.buscarPorCpf(null).isEmpty());
    }
}
