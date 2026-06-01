package integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.cliente.CadastrarCliente;
import application.ordemservico.AbrirOrdemDeServico;
import application.ordemservico.AprovarOrcamentoDaOS;
import application.ordemservico.ConcluirServico;
import application.ordemservico.EntregarVeiculo;
import application.ordemservico.ItemDeMaoDeObraInput;
import application.ordemservico.ItemDePecaInput;
import application.ordemservico.MontarOrcamentoDaOS;
import application.ordemservico.RegistrarDiagnosticoNaOS;
import application.ordemservico.RejeitarOrcamentoDaOS;
import application.peca.CadastrarPeca;
import application.veiculo.CadastrarVeiculo;
import domain.cliente.ClienteId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.StatusOrcamento;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.StatusOS;
import domain.peca.PecaId;
import domain.shared.CPF;
import domain.shared.Preco;
import domain.veiculo.Placa;
import domain.veiculo.VeiculoId;
import infrastructure.sqlite.ClienteSqliteRepository;
import infrastructure.sqlite.OficinaDatabase;
import infrastructure.sqlite.OrcamentoSqliteRepository;
import infrastructure.sqlite.OrdemDeServicoSqliteRepository;
import infrastructure.sqlite.PecaSqliteRepository;
import infrastructure.sqlite.VeiculoSqliteRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FluxoOficinaSqliteTest {

    @Test
    @DisplayName("fluxo completo contra SQLite: cadastros → diagnóstico → orçamento → aprovação → entrega")
    void fluxoCompletoAteEntrega() {
        try (OficinaDatabase database = new OficinaDatabase("jdbc:sqlite::memory:")) {
            ClienteSqliteRepository clienteRepo = new ClienteSqliteRepository(database);
            VeiculoSqliteRepository veiculoRepo = new VeiculoSqliteRepository(database);
            PecaSqliteRepository pecaRepo = new PecaSqliteRepository(database);
            OrcamentoSqliteRepository orcamentoRepo = new OrcamentoSqliteRepository(database);
            OrdemDeServicoSqliteRepository osRepo = new OrdemDeServicoSqliteRepository(database);

            CadastrarCliente cadastrarCliente = new CadastrarCliente(clienteRepo);
            CadastrarVeiculo cadastrarVeiculo = new CadastrarVeiculo(veiculoRepo, clienteRepo);
            CadastrarPeca cadastrarPeca = new CadastrarPeca(pecaRepo);
            AbrirOrdemDeServico abrirOS = new AbrirOrdemDeServico(osRepo, clienteRepo, veiculoRepo);
            RegistrarDiagnosticoNaOS registrarDiagnostico = new RegistrarDiagnosticoNaOS(osRepo);
            MontarOrcamentoDaOS montarOrcamento = new MontarOrcamentoDaOS(osRepo, orcamentoRepo, pecaRepo);
            AprovarOrcamentoDaOS aprovar = new AprovarOrcamentoDaOS(osRepo, orcamentoRepo);
            ConcluirServico concluir = new ConcluirServico(osRepo);
            EntregarVeiculo entregar = new EntregarVeiculo(osRepo);

            ClienteId clienteId = cadastrarCliente.executar(
                "Maria Silva", CPF.de("529.982.247-25"), "11912345678"
            );
            VeiculoId veiculoId = cadastrarVeiculo.executar(
                Placa.de("ABC1234"), "Fiat", "Uno", 2010, clienteId
            );
            PecaId filtroId = cadastrarPeca.executar("FLT-1001", "Filtro de óleo", Preco.deReais("50.00"), 10);
            PecaId oleoId = cadastrarPeca.executar("OLEO-5W30", "Óleo 5W30", Preco.deReais("80.00"), 20);

            OrdemDeServicoId osId = abrirOS.executar(clienteId, veiculoId);
            registrarDiagnostico.executar(osId, "Suspensão dianteira com folga, troca de óleo");

            OrcamentoId orcamentoId = montarOrcamento.executar(
                osId,
                List.of(
                    new ItemDePecaInput(filtroId, 1),
                    new ItemDePecaInput(oleoId, 4)
                ),
                List.of(
                    new ItemDeMaoDeObraInput("Troca de óleo e filtro", Preco.deReais("120.00"), 1)
                )
            );

            aprovar.executar(osId);
            concluir.executar(osId);
            entregar.executar(osId);

            // Verifica o estado final relendo do banco — prova que SQLite preservou tudo.
            OrdemDeServico osFinal = osRepo.buscarPorId(osId).orElseThrow();
            assertEquals(StatusOS.ENTREGUE, osFinal.status());
            assertEquals(clienteId, osFinal.clienteId());
            assertEquals(veiculoId, osFinal.veiculoId());
            assertEquals(Optional.of(orcamentoId), osFinal.orcamentoId());
            assertEquals(Optional.of(LocalDate.now()), osFinal.dataConclusao());
            assertEquals(Optional.of(LocalDate.now()), osFinal.dataEntrega());

            Orcamento orcamentoFinal = orcamentoRepo.buscarPorId(orcamentoId).orElseThrow();
            assertEquals(StatusOrcamento.APROVADO, orcamentoFinal.status());
            // 50×1 + 80×4 + 120×1 = 490
            assertEquals(Preco.deReais("490.00"), orcamentoFinal.total());
            assertEquals(3, orcamentoFinal.quantidadeDeItens());
        }
    }

    @Test
    @DisplayName("fluxo de rejeição contra SQLite preserva motivo nos dois agregados")
    void fluxoDeRejeicao() {
        try (OficinaDatabase database = new OficinaDatabase("jdbc:sqlite::memory:")) {
            ClienteSqliteRepository clienteRepo = new ClienteSqliteRepository(database);
            VeiculoSqliteRepository veiculoRepo = new VeiculoSqliteRepository(database);
            PecaSqliteRepository pecaRepo = new PecaSqliteRepository(database);
            OrcamentoSqliteRepository orcamentoRepo = new OrcamentoSqliteRepository(database);
            OrdemDeServicoSqliteRepository osRepo = new OrdemDeServicoSqliteRepository(database);

            CadastrarCliente cadastrarCliente = new CadastrarCliente(clienteRepo);
            CadastrarVeiculo cadastrarVeiculo = new CadastrarVeiculo(veiculoRepo, clienteRepo);
            CadastrarPeca cadastrarPeca = new CadastrarPeca(pecaRepo);
            AbrirOrdemDeServico abrirOS = new AbrirOrdemDeServico(osRepo, clienteRepo, veiculoRepo);
            RegistrarDiagnosticoNaOS registrarDiagnostico = new RegistrarDiagnosticoNaOS(osRepo);
            MontarOrcamentoDaOS montarOrcamento = new MontarOrcamentoDaOS(osRepo, orcamentoRepo, pecaRepo);
            RejeitarOrcamentoDaOS rejeitar = new RejeitarOrcamentoDaOS(osRepo, orcamentoRepo);

            ClienteId clienteId = cadastrarCliente.executar(
                "João Souza", CPF.de("111.444.777-35"), "11999998888"
            );
            VeiculoId veiculoId = cadastrarVeiculo.executar(
                Placa.de("XYZ1A23"), "Volkswagen", "Gol", 2018, clienteId
            );
            PecaId pastilhaId = cadastrarPeca.executar(
                "PST-2002", "Pastilha de freio", Preco.deReais("180.00"), 5
            );

            OrdemDeServicoId osId = abrirOS.executar(clienteId, veiculoId);
            registrarDiagnostico.executar(osId, "Freios com ruído ao acionar");
            OrcamentoId orcamentoId = montarOrcamento.executar(
                osId,
                List.of(new ItemDePecaInput(pastilhaId, 4)),
                List.of(new ItemDeMaoDeObraInput("Troca de pastilhas", Preco.deReais("250.00"), 1))
            );

            rejeitar.executar(osId, "Cliente preferiu segunda opinião");

            OrdemDeServico osFinal = osRepo.buscarPorId(osId).orElseThrow();
            assertEquals(StatusOS.REJEITADA, osFinal.status());
            assertEquals(Optional.of("Cliente preferiu segunda opinião"), osFinal.motivoRejeicao());

            Orcamento orcamentoFinal = orcamentoRepo.buscarPorId(orcamentoId).orElseThrow();
            assertEquals(StatusOrcamento.REJEITADO, orcamentoFinal.status());
            assertEquals(Optional.of("Cliente preferiu segunda opinião"), orcamentoFinal.motivoRejeicao());
            assertTrue(orcamentoFinal.quantidadeDeItens() > 0);
        }
    }
}
