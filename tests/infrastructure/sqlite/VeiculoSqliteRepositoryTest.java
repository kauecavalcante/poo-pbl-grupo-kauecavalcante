package infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.shared.CPF;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VeiculoSqliteRepositoryTest {

    private OficinaDatabase database;
    private VeiculoSqliteRepository repo;
    private ClienteSqliteRepository clienteRepo;
    private ClienteId DONO;

    @BeforeEach
    void setup() {
        database = new OficinaDatabase("jdbc:sqlite::memory:");
        repo = new VeiculoSqliteRepository(database);
        clienteRepo = new ClienteSqliteRepository(database);
        Cliente cliente = Cliente.novo("Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        clienteRepo.salvar(cliente);
        DONO = cliente.id();
    }

    @AfterEach
    void teardown() {
        database.close();
    }

    @Test
    @DisplayName("salvar e buscar por id devolve veículo equivalente")
    void roundtripPorId() {
        Veiculo original = Veiculo.novo(Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO);
        repo.salvar(original);

        Veiculo recuperado = repo.buscarPorId(original.id()).orElseThrow();
        assertEquals(original.id(), recuperado.id());
        assertEquals(Placa.de("ABC1234"), recuperado.placa());
        assertEquals("Fiat", recuperado.marca());
        assertEquals("Uno", recuperado.modelo());
        assertEquals(2010, recuperado.ano());
        assertEquals(DONO, recuperado.dono());
    }

    @Test
    @DisplayName("salvar duas vezes com mesmo id atualiza a versão persistida")
    void salvarSobrescreve() {
        VeiculoId id = VeiculoId.novo();
        repo.salvar(Veiculo.reconstituir(id, Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO));
        repo.salvar(Veiculo.reconstituir(id, Placa.de("ABC1234"), "Volkswagen", "Gol", 2015, DONO));

        Veiculo recuperado = repo.buscarPorId(id).orElseThrow();
        assertEquals("Volkswagen", recuperado.marca());
        assertEquals(2015, recuperado.ano());
    }

    @Test
    @DisplayName("buscar por id inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorId(VeiculoId.novo()));
    }

    @Test
    @DisplayName("buscar por id null retorna Optional.empty")
    void buscarPorIdNulo() {
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("buscar por placa encontra o veículo cadastrado")
    void buscarPorPlacaFeliz() {
        Veiculo v = Veiculo.novo(Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO);
        repo.salvar(v);
        assertEquals(v.id(), repo.buscarPorPlaca(Placa.de("ABC1234")).orElseThrow().id());
    }

    @Test
    @DisplayName("buscar por placa inexistente retorna Optional.empty")
    void buscarPorPlacaInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorPlaca(Placa.de("XYZ4321")));
    }

    @Test
    @DisplayName("buscar por placa null retorna Optional.empty")
    void buscarPorPlacaNulo() {
        assertTrue(repo.buscarPorPlaca(null).isEmpty());
    }

    @Test
    @DisplayName("listarPorDono retorna todos os veículos do dono")
    void listarPorDonoMultiplos() {
        Veiculo a = Veiculo.novo(Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO);
        Veiculo b = Veiculo.novo(Placa.de("XYZ4321"), "Volkswagen", "Gol", 2015, DONO);
        repo.salvar(a);
        repo.salvar(b);

        List<Veiculo> resultado = repo.listarPorDono(DONO);
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("listarPorDono ignora veículos de outro dono")
    void listarPorDonoIgnoraOutros() {
        Cliente outroCliente = Cliente.novo("João Souza", CPF.de("111.444.777-35"), "11999998888");
        clienteRepo.salvar(outroCliente);

        repo.salvar(Veiculo.novo(Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO));
        repo.salvar(Veiculo.novo(Placa.de("XYZ4321"), "Fiat", "Uno", 2010, outroCliente.id()));

        List<Veiculo> resultado = repo.listarPorDono(DONO);
        assertEquals(1, resultado.size());
        assertEquals(DONO, resultado.get(0).dono());
    }

    @Test
    @DisplayName("listarPorDono sem matches retorna lista vazia")
    void listarPorDonoVazio() {
        assertEquals(List.of(), repo.listarPorDono(ClienteId.novo()));
    }

    @Test
    @DisplayName("listarPorDono com null retorna lista vazia")
    void listarPorDonoNulo() {
        assertEquals(List.of(), repo.listarPorDono(null));
    }

    @Test
    @DisplayName("salvar dois veículos com mesma placa viola UNIQUE e lança FalhaDePersistencia")
    void placaDuplicadaLancaFalha() {
        repo.salvar(Veiculo.novo(Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO));
        Veiculo segundo = Veiculo.novo(Placa.de("ABC1234"), "Outro", "Modelo", 2015, DONO);
        assertThrows(FalhaDePersistencia.class, () -> repo.salvar(segundo));
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException")
    void salvarNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("veiculo", ex.getMessage());
    }
}
