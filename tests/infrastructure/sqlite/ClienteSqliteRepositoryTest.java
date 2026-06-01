package infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.shared.CPF;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteSqliteRepositoryTest {

    private OficinaDatabase database;
    private ClienteSqliteRepository repo;

    @BeforeEach
    void setup() {
        database = new OficinaDatabase("jdbc:sqlite::memory:");
        repo = new ClienteSqliteRepository(database);
    }

    @AfterEach
    void teardown() {
        database.close();
    }

    @Test
    @DisplayName("salvar e buscar por id devolve cliente equivalente ao salvo")
    void roundtripPorId() {
        Cliente original = Cliente.novo("Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        repo.salvar(original);

        Cliente recuperado = repo.buscarPorId(original.id()).orElseThrow();
        assertEquals(original.id(), recuperado.id());
        assertEquals("Maria Silva", recuperado.nome());
        assertEquals(CPF.de("529.982.247-25"), recuperado.cpf());
        assertEquals("11912345678", recuperado.telefone());
    }

    @Test
    @DisplayName("salvar duas vezes com mesmo id atualiza a versão persistida")
    void salvarSobrescreve() {
        ClienteId id = ClienteId.novo();
        repo.salvar(Cliente.reconstituir(id, "Maria Silva", CPF.de("529.982.247-25"), "11912345678"));
        repo.salvar(Cliente.reconstituir(id, "Maria S. Pereira", CPF.de("529.982.247-25"), "11999998888"));

        Cliente recuperado = repo.buscarPorId(id).orElseThrow();
        assertEquals("Maria S. Pereira", recuperado.nome());
        assertEquals("11999998888", recuperado.telefone());
    }

    @Test
    @DisplayName("buscar por id inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorId(ClienteId.novo()));
    }

    @Test
    @DisplayName("buscar por id null retorna Optional.empty")
    void buscarPorIdNulo() {
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("buscar por cpf encontra o cliente cadastrado")
    void buscarPorCpfFeliz() {
        Cliente c1 = Cliente.novo("Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        Cliente c2 = Cliente.novo("João Souza", CPF.de("111.444.777-35"), "11999998888");
        repo.salvar(c1);
        repo.salvar(c2);

        Cliente recuperado = repo.buscarPorCpf(CPF.de("111.444.777-35")).orElseThrow();
        assertEquals(c2.id(), recuperado.id());
    }

    @Test
    @DisplayName("buscar por cpf inexistente retorna Optional.empty")
    void buscarPorCpfInexistente() {
        repo.salvar(Cliente.novo("Maria Silva", CPF.de("529.982.247-25"), "11912345678"));
        assertEquals(Optional.empty(), repo.buscarPorCpf(CPF.de("111.444.777-35")));
    }

    @Test
    @DisplayName("buscar por cpf null retorna Optional.empty")
    void buscarPorCpfNulo() {
        assertTrue(repo.buscarPorCpf(null).isEmpty());
    }

    @Test
    @DisplayName("salvar dois clientes com mesmo cpf viola UNIQUE e lança FalhaDePersistencia")
    void cpfDuplicadoLancaFalha() {
        repo.salvar(Cliente.novo("Maria Silva", CPF.de("529.982.247-25"), "11912345678"));
        Cliente segundo = Cliente.novo("Outro nome", CPF.de("529.982.247-25"), "11999998888");
        assertThrows(FalhaDePersistencia.class, () -> repo.salvar(segundo));
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException")
    void salvarNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("cliente", ex.getMessage());
    }

    @Test
    @DisplayName("construtor rejeita database null")
    void rejeitaDatabaseNulo() {
        assertThrows(NullPointerException.class, () -> new ClienteSqliteRepository(null));
    }
}
