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
import infrastructure.memoria.ClienteEmMemoriaRepository;
import infrastructure.memoria.OrcamentoEmMemoriaRepository;
import infrastructure.memoria.OrdemDeServicoEmMemoriaRepository;
import infrastructure.memoria.PecaEmMemoriaRepository;
import infrastructure.memoria.VeiculoEmMemoriaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FluxoOficinaTest {

    @Test
    @DisplayName("fluxo completo: cadastros → abertura → diagnóstico → orçamento → aprovação → conclusão → entrega")
    void fluxoCompletoAteEntrega() {
        // --- Repositórios reais (sem mocks) ---
        ClienteEmMemoriaRepository clienteRepo = new ClienteEmMemoriaRepository();
        VeiculoEmMemoriaRepository veiculoRepo = new VeiculoEmMemoriaRepository();
        PecaEmMemoriaRepository pecaRepo = new PecaEmMemoriaRepository();
        OrcamentoEmMemoriaRepository orcamentoRepo = new OrcamentoEmMemoriaRepository();
        OrdemDeServicoEmMemoriaRepository osRepo = new OrdemDeServicoEmMemoriaRepository();

        // --- Casos de uso amarrados aos repositórios ---
        CadastrarCliente cadastrarCliente = new CadastrarCliente(clienteRepo);
        CadastrarVeiculo cadastrarVeiculo = new CadastrarVeiculo(veiculoRepo, clienteRepo);
        CadastrarPeca cadastrarPeca = new CadastrarPeca(pecaRepo);
        AbrirOrdemDeServico abrirOS = new AbrirOrdemDeServico(osRepo, clienteRepo, veiculoRepo);
        RegistrarDiagnosticoNaOS registrarDiagnostico = new RegistrarDiagnosticoNaOS(osRepo);
        MontarOrcamentoDaOS montarOrcamento = new MontarOrcamentoDaOS(osRepo, orcamentoRepo, pecaRepo);
        AprovarOrcamentoDaOS aprovar = new AprovarOrcamentoDaOS(osRepo, orcamentoRepo);
        ConcluirServico concluir = new ConcluirServico(osRepo);
        EntregarVeiculo entregar = new EntregarVeiculo(osRepo);

        // --- Passo 1: cadastrar cliente ---
        ClienteId clienteId = cadastrarCliente.executar(
            "Maria Silva", CPF.de("529.982.247-25"), "11912345678"
        );

        // --- Passo 2: cadastrar veículo do cliente ---
        VeiculoId veiculoId = cadastrarVeiculo.executar(
            Placa.de("ABC1234"), "Fiat", "Uno", 2010, clienteId
        );

        // --- Passo 3: cadastrar duas peças do catálogo ---
        PecaId filtroId = cadastrarPeca.executar("FLT-1001", "Filtro de óleo", Preco.deReais("50.00"), 10);
        PecaId oleoId = cadastrarPeca.executar("OLEO-5W30", "Óleo 5W30", Preco.deReais("80.00"), 20);

        // --- Passo 4: abrir OS para esse cliente e veículo ---
        OrdemDeServicoId osId = abrirOS.executar(clienteId, veiculoId);
        assertEquals(StatusOS.RECEBIDA, osRepo.buscarPorId(osId).orElseThrow().status());

        // --- Passo 5: registrar diagnóstico ---
        registrarDiagnostico.executar(osId, "Suspensão dianteira com folga, troca de óleo");

        // --- Passo 6: montar orçamento com peças + mão de obra ---
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

        // OS deve estar AGUARDANDO_APROVACAO após anexar o orçamento
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, osRepo.buscarPorId(osId).orElseThrow().status());

        // --- Passo 7: aprovar orçamento ---
        aprovar.executar(osId);

        Orcamento orcamentoAprovado = orcamentoRepo.buscarPorId(orcamentoId).orElseThrow();
        assertEquals(StatusOrcamento.APROVADO, orcamentoAprovado.status());
        // 50×1 + 80×4 + 120×1 = 50 + 320 + 120 = 490
        assertEquals(Preco.deReais("490.00"), orcamentoAprovado.total());

        assertEquals(StatusOS.EM_EXECUCAO, osRepo.buscarPorId(osId).orElseThrow().status());

        // --- Passo 8: concluir serviço ---
        concluir.executar(osId);

        OrdemDeServico osConcluida = osRepo.buscarPorId(osId).orElseThrow();
        assertEquals(StatusOS.CONCLUIDA, osConcluida.status());
        assertEquals(Optional.of(LocalDate.now()), osConcluida.dataConclusao());

        // --- Passo 9: entregar veículo ---
        entregar.executar(osId);

        OrdemDeServico osEntregue = osRepo.buscarPorId(osId).orElseThrow();
        assertEquals(StatusOS.ENTREGUE, osEntregue.status());
        assertEquals(Optional.of(LocalDate.now()), osEntregue.dataEntrega());

        // --- Estado final consistente em todos os agregados ---
        assertEquals(clienteId, osEntregue.clienteId());
        assertEquals(veiculoId, osEntregue.veiculoId());
        assertEquals(Optional.of(orcamentoId), osEntregue.orcamentoId());
        assertTrue(osEntregue.dataAbertura().isEqual(LocalDate.now()) || osEntregue.dataAbertura().isBefore(LocalDate.now()));
        assertTrue(osEntregue.dataAbertura().compareTo(osEntregue.dataConclusao().orElseThrow()) <= 0);
        assertTrue(osEntregue.dataConclusao().orElseThrow().compareTo(osEntregue.dataEntrega().orElseThrow()) <= 0);
    }

    @Test
    @DisplayName("fluxo de rejeição: orçamento rejeitado leva OS ao status REJEITADA com motivo registrado")
    void fluxoDeRejeicao() {
        ClienteEmMemoriaRepository clienteRepo = new ClienteEmMemoriaRepository();
        VeiculoEmMemoriaRepository veiculoRepo = new VeiculoEmMemoriaRepository();
        PecaEmMemoriaRepository pecaRepo = new PecaEmMemoriaRepository();
        OrcamentoEmMemoriaRepository orcamentoRepo = new OrcamentoEmMemoriaRepository();
        OrdemDeServicoEmMemoriaRepository osRepo = new OrdemDeServicoEmMemoriaRepository();

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

        OrdemDeServico osRejeitada = osRepo.buscarPorId(osId).orElseThrow();
        assertEquals(StatusOS.REJEITADA, osRejeitada.status());
        assertEquals(Optional.of("Cliente preferiu segunda opinião"), osRejeitada.motivoRejeicao());

        Orcamento orcamentoRejeitado = orcamentoRepo.buscarPorId(orcamentoId).orElseThrow();
        assertEquals(StatusOrcamento.REJEITADO, orcamentoRejeitado.status());
    }
}
