package infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.ordemservico.Cancelada;
import domain.ordemservico.Concluida;
import domain.ordemservico.EmExecucao;
import domain.ordemservico.Entregue;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.Recebida;
import domain.ordemservico.Rejeitada;
import domain.ordemservico.StatusOS;
import domain.shared.CPF;
import domain.shared.Preco;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemDeServicoSqliteRepositoryTest {

    private OficinaDatabase database;
    private OrdemDeServicoSqliteRepository repo;
    private OrcamentoSqliteRepository orcamentoRepo;
    private ClienteId clienteId;
    private VeiculoId veiculoId;

    @BeforeEach
    void setup() {
        database = new OficinaDatabase("jdbc:sqlite::memory:");
        repo = new OrdemDeServicoSqliteRepository(database);
        orcamentoRepo = new OrcamentoSqliteRepository(database);

        // FKs exigem cliente e veículo já cadastrados na base.
        ClienteSqliteRepository clienteRepo = new ClienteSqliteRepository(database);
        VeiculoSqliteRepository veiculoRepo = new VeiculoSqliteRepository(database);
        Cliente cliente = Cliente.novo("Maria Silva", CPF.de("529.982.247-25"), "11912345678");
        clienteRepo.salvar(cliente);
        clienteId = cliente.id();
        Veiculo veiculo = Veiculo.novo(Placa.de("ABC1234"), "Fiat", "Uno", 2010, clienteId);
        veiculoRepo.salvar(veiculo);
        veiculoId = veiculo.id();
    }

    @AfterEach
    void teardown() {
        database.close();
    }

    @Test
    @DisplayName("OS recém aberta é persistida em RECEBIDA com data de abertura")
    void roundtripRecebida() {
        OrdemDeServico os = OrdemDeServico.abrir(clienteId, veiculoId);
        repo.salvar(os);

        OrdemDeServico recuperada = repo.buscarPorId(os.id()).orElseThrow();
        assertEquals(os.id(), recuperada.id());
        assertEquals(StatusOS.RECEBIDA, recuperada.status());
        assertEquals(LocalDate.now(), recuperada.dataAbertura());
        assertEquals(Optional.empty(), recuperada.diagnostico());
        assertEquals(Optional.empty(), recuperada.orcamentoId());
        assertEquals(Optional.empty(), recuperada.dataConclusao());
        assertEquals(Optional.empty(), recuperada.dataEntrega());
    }

    @Test
    @DisplayName("OS após registrar diagnóstico preserva o texto em roundtrip")
    void roundtripComDiagnostico() {
        OrdemDeServico os = OrdemDeServico.abrir(clienteId, veiculoId);
        os.registrarDiagnostico("Suspensão dianteira com folga");
        repo.salvar(os);

        OrdemDeServico recuperada = repo.buscarPorId(os.id()).orElseThrow();
        assertEquals(Optional.of("Suspensão dianteira com folga"), recuperada.diagnostico());
    }

    @Test
    @DisplayName("OS em AGUARDANDO_APROVACAO preserva o orcamentoId anexado")
    void roundtripComOrcamento() {
        OrdemDeServico os = OrdemDeServico.abrir(clienteId, veiculoId);
        os.registrarDiagnostico("Diagnóstico");
        Orcamento orcamento = Orcamento.novo();
        orcamento.adicionarItemDeMaoDeObra("Troca", Preco.deReais("100.00"), 1);
        orcamento.enviar();
        orcamentoRepo.salvar(orcamento);
        os.anexarOrcamento(orcamento.id());
        repo.salvar(os);

        OrdemDeServico recuperada = repo.buscarPorId(os.id()).orElseThrow();
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, recuperada.status());
        assertEquals(Optional.of(orcamento.id()), recuperada.orcamentoId());
    }

    @Test
    @DisplayName("OS CONCLUIDA preserva dataConclusao em roundtrip")
    void roundtripConcluida() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        OrdemDeServico os = OrdemDeServico.reconstituir(
            id, clienteId, veiculoId,
            null, "Diagnóstico de teste", new Concluida(),
            LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 15), null
        );
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOS.CONCLUIDA, recuperada.status());
        assertEquals(Optional.of(LocalDate.of(2024, 1, 15)), recuperada.dataConclusao());
        assertTrue(recuperada.dataEntrega().isEmpty());
    }

    @Test
    @DisplayName("OS ENTREGUE preserva ambas as datas finais em roundtrip")
    void roundtripEntregue() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        OrdemDeServico os = OrdemDeServico.reconstituir(
            id, clienteId, veiculoId,
            null, "Diagnóstico de teste", new Entregue(),
            LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16)
        );
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOS.ENTREGUE, recuperada.status());
        assertEquals(Optional.of(LocalDate.of(2024, 1, 15)), recuperada.dataConclusao());
        assertEquals(Optional.of(LocalDate.of(2024, 1, 16)), recuperada.dataEntrega());
    }

    @Test
    @DisplayName("OS REJEITADA preserva motivo_rejeicao em roundtrip")
    void roundtripRejeitada() {
        Orcamento orcamento = Orcamento.novo();
        orcamento.adicionarItemDeMaoDeObra("Troca", Preco.deReais("100.00"), 1);
        orcamento.enviar();
        orcamentoRepo.salvar(orcamento);

        OrdemDeServicoId id = OrdemDeServicoId.novo();
        OrdemDeServico os = OrdemDeServico.reconstituir(
            id, clienteId, veiculoId,
            orcamento.id(), "Diagnóstico de teste", new Rejeitada("Cliente desistiu"),
            LocalDate.of(2024, 1, 10), null, null
        );
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOS.REJEITADA, recuperada.status());
        assertEquals(Optional.of("Cliente desistiu"), recuperada.motivoRejeicao());
    }

    @Test
    @DisplayName("OS CANCELADA preserva motivo_cancelamento em roundtrip")
    void roundtripCancelada() {
        OrdemDeServico os = OrdemDeServico.abrir(clienteId, veiculoId);
        os.cancelar("Cliente preferiu outra oficina");
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(os.id()).orElseThrow();
        assertEquals(StatusOS.CANCELADA, recuperada.status());
        assertEquals(Optional.of("Cliente preferiu outra oficina"), recuperada.motivoCancelamento());
    }

    @Test
    @DisplayName("OS EM_EXECUCAO sem datas finais é persistida sem nulos espúrios")
    void roundtripEmExecucao() {
        Orcamento orcamento = Orcamento.novo();
        orcamento.adicionarItemDeMaoDeObra("Troca", Preco.deReais("100.00"), 1);
        orcamento.enviar();
        orcamentoRepo.salvar(orcamento);

        OrdemDeServicoId id = OrdemDeServicoId.novo();
        OrdemDeServico os = OrdemDeServico.reconstituir(
            id, clienteId, veiculoId,
            orcamento.id(), "Diagnóstico", new EmExecucao(),
            LocalDate.of(2024, 1, 10), null, null
        );
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOS.EM_EXECUCAO, recuperada.status());
        assertEquals(Optional.empty(), recuperada.dataConclusao());
        assertEquals(Optional.empty(), recuperada.dataEntrega());
    }

    @Test
    @DisplayName("salvar duas vezes atualiza o estado persistido")
    void atualizar() {
        OrdemDeServico os = OrdemDeServico.abrir(clienteId, veiculoId);
        repo.salvar(os);
        os.registrarDiagnostico("Diagnóstico atualizado");
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(os.id()).orElseThrow();
        assertEquals(Optional.of("Diagnóstico atualizado"), recuperada.diagnostico());
    }

    @Test
    @DisplayName("buscar por id inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorId(OrdemDeServicoId.novo()));
    }

    @Test
    @DisplayName("buscar por id null retorna Optional.empty")
    void buscarPorIdNulo() {
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException")
    void salvarNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("ordemDeServico", ex.getMessage());
    }

    @Test
    @DisplayName("Recebida sem dados extras é reconstituído corretamente do banco")
    void reconstituirRecebida() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        OrdemDeServico os = OrdemDeServico.reconstituir(
            id, clienteId, veiculoId,
            null, null, new Recebida(),
            LocalDate.of(2024, 1, 10), null, null
        );
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOS.RECEBIDA, recuperada.status());
        assertTrue(recuperada.diagnostico().isEmpty());
        assertTrue(recuperada.orcamentoId().isEmpty());
    }

    @Test
    @DisplayName("Cancelada via reconstituir externo preserva motivo")
    void reconstituirCancelada() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        OrdemDeServico os = OrdemDeServico.reconstituir(
            id, clienteId, veiculoId,
            null, null, new Cancelada("Motivo persistido"),
            LocalDate.of(2024, 1, 10), null, null
        );
        repo.salvar(os);
        OrdemDeServico recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals(Optional.of("Motivo persistido"), recuperada.motivoCancelamento());
    }
}
