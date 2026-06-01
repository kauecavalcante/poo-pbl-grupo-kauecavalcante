package infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.ClienteId;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VeiculoEmMemoriaRepositoryTest {

    private static final ClienteId DONO = ClienteId.novo();

    private Veiculo veiculo(String placa, ClienteId dono) {
        return Veiculo.novo(Placa.de(placa), "Fiat", "Uno", 2010, dono);
    }

    @Test
    @DisplayName("salvar e buscarPorId devolve a entidade exata salva")
    void salvarEBuscar() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        Veiculo v = veiculo("ABC1234", DONO);
        repo.salvar(v);
        assertSame(v, repo.buscarPorId(v.id()).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorId com ID inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(VeiculoId.novo()));
    }

    @Test
    @DisplayName("buscarPorId com null retorna Optional.empty")
    void buscarPorIdNulo() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("salvar com mesmo ID substitui a versão anterior")
    void atualizar() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        VeiculoId id = VeiculoId.novo();
        Veiculo antes = Veiculo.reconstituir(id, Placa.de("ABC1234"), "Fiat", "Uno", 2010, DONO);
        Veiculo depois = Veiculo.reconstituir(id, Placa.de("ABC1234"), "Volkswagen", "Gol", 2015, DONO);
        repo.salvar(antes);
        repo.salvar(depois);
        assertSame(depois, repo.buscarPorId(id).orElseThrow());
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException com mensagem veiculo")
    void salvarNulo() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("veiculo", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorPlaca encontra veículo cadastrado")
    void buscarPorPlacaFeliz() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        Veiculo v = veiculo("ABC1234", DONO);
        repo.salvar(v);
        assertSame(v, repo.buscarPorPlaca(Placa.de("ABC1234")).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorPlaca com placa não cadastrada retorna Optional.empty")
    void buscarPorPlacaInexistente() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        repo.salvar(veiculo("ABC1234", DONO));
        assertEquals(Optional.empty(), repo.buscarPorPlaca(Placa.de("XYZ4321")));
    }

    @Test
    @DisplayName("buscarPorPlaca com null retorna Optional.empty")
    void buscarPorPlacaNulo() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        assertTrue(repo.buscarPorPlaca(null).isEmpty());
    }

    @Test
    @DisplayName("listarPorDono retorna todos os veículos do dono")
    void listarPorDonoComMultiplosVeiculos() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        Veiculo a = veiculo("ABC1234", DONO);
        Veiculo b = veiculo("XYZ4321", DONO);
        repo.salvar(a);
        repo.salvar(b);
        List<Veiculo> resultado = repo.listarPorDono(DONO);
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(a));
        assertTrue(resultado.contains(b));
    }

    @Test
    @DisplayName("listarPorDono ignora veículos de outros donos")
    void listarPorDonoIgnoraVeiculosDeOutroDono() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        ClienteId outroDono = ClienteId.novo();
        Veiculo doDono = veiculo("ABC1234", DONO);
        Veiculo deOutro = veiculo("XYZ4321", outroDono);
        repo.salvar(doDono);
        repo.salvar(deOutro);

        List<Veiculo> resultado = repo.listarPorDono(DONO);
        assertEquals(1, resultado.size());
        assertSame(doDono, resultado.get(0));
    }

    @Test
    @DisplayName("listarPorDono sem matches retorna lista vazia")
    void listarPorDonoSemMatches() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        assertEquals(List.of(), repo.listarPorDono(ClienteId.novo()));
    }

    @Test
    @DisplayName("listarPorDono com null retorna lista vazia")
    void listarPorDonoNulo() {
        VeiculoEmMemoriaRepository repo = new VeiculoEmMemoriaRepository();
        repo.salvar(veiculo("ABC1234", DONO));
        assertEquals(List.of(), repo.listarPorDono(null));
    }
}
