package domain.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.cliente.ClienteId;
import domain.orcamento.OrcamentoId;
import domain.veiculo.VeiculoId;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrdemDeServicoTest {

    private static final ClienteId CLIENTE_ID = ClienteId.novo();
    private static final VeiculoId VEICULO_ID = VeiculoId.novo();
    private static final OrcamentoId ORCAMENTO_ID = OrcamentoId.novo();
    private static final String DIAGNOSTICO = "Suspensão dianteira com folga";
    private static final String MOTIVO = "Cliente desistiu";

    @Nested
    @DisplayName("Abertura de OS")
    class Abertura {

        @Test
        @DisplayName("abrir gera identidade")
        void abrirGeraIdentidade() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertNotNull(os.id());
        }

        @Test
        @DisplayName("abrir expõe cliente e veículo informados")
        void abrirExpoeClienteEVeiculo() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertSame(CLIENTE_ID, os.clienteId());
            assertSame(VEICULO_ID, os.veiculoId());
        }

        @Test
        @DisplayName("abrir começa no estado RECEBIDA")
        void abrirComecaEmRecebida() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(StatusOS.RECEBIDA, os.status());
        }

        @Test
        @DisplayName("abrir registra a data de hoje como dataAbertura")
        void abrirRegistraDataAbertura() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(LocalDate.now(), os.dataAbertura());
        }

        @Test
        @DisplayName("abrir começa sem orçamento, diagnóstico, conclusão ou entrega")
        void abrirComecaSemDadosOpcionais() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(Optional.empty(), os.orcamentoId());
            assertEquals(Optional.empty(), os.diagnostico());
            assertEquals(Optional.empty(), os.dataConclusao());
            assertEquals(Optional.empty(), os.dataEntrega());
            assertEquals(Optional.empty(), os.motivoRejeicao());
            assertEquals(Optional.empty(), os.motivoCancelamento());
        }

        @Test
        @DisplayName("duas OS abertas recebem IDs distintos")
        void abrirGeraIdsDistintos() {
            OrdemDeServico a = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            OrdemDeServico b = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(false, a.id().equals(b.id()));
        }

        @Test
        @DisplayName("rejeita cliente nulo")
        void rejeitaClienteNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.abrir(null, VEICULO_ID)
            );
            assertEquals("cliente da OS não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita veículo nulo")
        void rejeitaVeiculoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.abrir(CLIENTE_ID, null)
            );
            assertEquals("veículo da OS não pode ser nulo", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Registro de diagnóstico")
    class Diagnostico {

        @Test
        @DisplayName("registrarDiagnostico armazena texto válido com trim")
        void registrarDiagnosticoArmazena() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico("   Suspensão dianteira com folga   ");
            assertEquals(Optional.of("Suspensão dianteira com folga"), os.diagnostico());
        }

        @Test
        @DisplayName("registrarDiagnostico rejeita texto nulo")
        void registrarDiagnosticoRejeitaNulo() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> os.registrarDiagnostico(null)
            );
            assertEquals("diagnóstico não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("registrarDiagnostico rejeita texto vazio ou só espaços")
        void registrarDiagnosticoRejeitaVazio() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertThrows(IllegalArgumentException.class, () -> os.registrarDiagnostico("   "));
        }

        @Test
        @DisplayName("registrarDiagnostico rejeita texto com menos de 5 caracteres após trim")
        void registrarDiagnosticoRejeitaCurto() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> os.registrarDiagnostico("ABC")
            );
            assertEquals("diagnóstico deve ter ao menos 5 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("registrarDiagnostico falha não modifica o diagnóstico já existente")
        void registroFalhaPreservaDiagnostico() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico("Suspensão com folga");
            assertThrows(IllegalArgumentException.class, () -> os.registrarDiagnostico(""));
            assertEquals(Optional.of("Suspensão com folga"), os.diagnostico());
        }
    }

    @Nested
    @DisplayName("Anexação de orçamento")
    class AnexacaoDeOrcamento {

        @Test
        @DisplayName("anexarOrcamento exige diagnóstico previamente registrado")
        void anexarSemDiagnostico() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> os.anexarOrcamento(ORCAMENTO_ID)
            );
            assertEquals("diagnóstico precisa estar registrado antes de anexar orçamento", ex.getMessage());
        }

        @Test
        @DisplayName("anexarOrcamento após diagnóstico armazena o ID e transita para AGUARDANDO_APROVACAO")
        void anexarComDiagnosticoTransita() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            assertEquals(Optional.of(ORCAMENTO_ID), os.orcamentoId());
            assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.status());
        }

        @Test
        @DisplayName("anexarOrcamento rejeita orcamentoId nulo")
        void anexarRejeitaOrcamentoIdNulo() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> os.anexarOrcamento(null)
            );
            assertEquals("orcamentoId não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("anexarOrcamento bloqueado em estados diferentes de RECEBIDA")
        void anexarBloqueadoForaDeRecebida() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> os.anexarOrcamento(OrcamentoId.novo())
            );
            assertEquals("orçamento só pode ser anexado em OS no estado RECEBIDA", ex.getMessage());
        }

        @Test
        @DisplayName("após anexar, registrarDiagnostico fica bloqueado")
        void registrarDiagnosticoBloqueadoAposAnexar() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> os.registrarDiagnostico("Novo diagnóstico")
            );
            assertEquals("diagnóstico só pode ser registrado em OS no estado RECEBIDA", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Aprovação e rejeição")
    class AprovacaoRejeicao {

        @Test
        @DisplayName("aprovar a partir de AGUARDANDO_APROVACAO transita para EM_EXECUCAO")
        void aprovarTransita() {
            OrdemDeServico os = aguardandoAprovacao();
            os.aprovar();
            assertEquals(StatusOS.EM_EXECUCAO, os.status());
        }

        @Test
        @DisplayName("rejeitar a partir de AGUARDANDO_APROVACAO transita para REJEITADA")
        void rejeitarTransita() {
            OrdemDeServico os = aguardandoAprovacao();
            os.rejeitar(MOTIVO);
            assertEquals(StatusOS.REJEITADA, os.status());
            assertEquals(Optional.of(MOTIVO), os.motivoRejeicao());
        }

        @Test
        @DisplayName("aprovar em RECEBIDA é bloqueado")
        void aprovarEmRecebidaBloqueado() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertThrows(IllegalStateException.class, os::aprovar);
        }

        @Test
        @DisplayName("rejeitar em RECEBIDA é bloqueado")
        void rejeitarEmRecebidaBloqueado() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertThrows(IllegalStateException.class, () -> os.rejeitar(MOTIVO));
        }

        @Test
        @DisplayName("aprovar duas vezes é bloqueado")
        void aprovarDuasVezesBloqueado() {
            OrdemDeServico os = aguardandoAprovacao();
            os.aprovar();
            assertThrows(IllegalStateException.class, os::aprovar);
        }

        @Test
        @DisplayName("rejeitar com motivo inválido lança IllegalArgumentException")
        void rejeitarComMotivoInvalido() {
            OrdemDeServico os = aguardandoAprovacao();
            assertThrows(IllegalArgumentException.class, () -> os.rejeitar(null));
            assertThrows(IllegalArgumentException.class, () -> os.rejeitar("AB"));
        }

        @Test
        @DisplayName("motivoRejeicao permanece vazio fora de REJEITADA")
        void motivoRejeicaoVazioForaDeRejeitada() {
            OrdemDeServico os = aguardandoAprovacao();
            assertEquals(Optional.empty(), os.motivoRejeicao());
            os.aprovar();
            assertEquals(Optional.empty(), os.motivoRejeicao());
        }

        private OrdemDeServico aguardandoAprovacao() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            return os;
        }
    }

    @Nested
    @DisplayName("Conclusão e entrega")
    class ConclusaoEEntrega {

        @Test
        @DisplayName("concluir a partir de EM_EXECUCAO transita para CONCLUIDA e grava dataConclusao")
        void concluirTransitaEGravaData() {
            OrdemDeServico os = emExecucao();
            os.concluir();
            assertEquals(StatusOS.CONCLUIDA, os.status());
            assertEquals(Optional.of(LocalDate.now()), os.dataConclusao());
        }

        @Test
        @DisplayName("entregar a partir de CONCLUIDA transita para ENTREGUE e grava dataEntrega")
        void entregarTransitaEGravaData() {
            OrdemDeServico os = emExecucao();
            os.concluir();
            os.entregar();
            assertEquals(StatusOS.ENTREGUE, os.status());
            assertEquals(Optional.of(LocalDate.now()), os.dataEntrega());
        }

        @Test
        @DisplayName("concluir em RECEBIDA é bloqueado")
        void concluirEmRecebidaBloqueado() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertThrows(IllegalStateException.class, os::concluir);
        }

        @Test
        @DisplayName("entregar em EM_EXECUCAO é bloqueado (precisa passar por CONCLUIDA)")
        void entregarEmExecucaoBloqueado() {
            OrdemDeServico os = emExecucao();
            assertThrows(IllegalStateException.class, os::entregar);
        }

        @Test
        @DisplayName("concluir duas vezes é bloqueado")
        void concluirDuasVezesBloqueado() {
            OrdemDeServico os = emExecucao();
            os.concluir();
            assertThrows(IllegalStateException.class, os::concluir);
        }

        @Test
        @DisplayName("entregar duas vezes é bloqueado (terminal)")
        void entregarDuasVezesBloqueado() {
            OrdemDeServico os = emExecucao();
            os.concluir();
            os.entregar();
            assertThrows(IllegalStateException.class, os::entregar);
        }

        @Test
        @DisplayName("dataConclusao continua presente após entregar")
        void dataConclusaoPersisteAposEntregar() {
            OrdemDeServico os = emExecucao();
            os.concluir();
            LocalDate conclusao = os.dataConclusao().orElseThrow();
            os.entregar();
            assertEquals(Optional.of(conclusao), os.dataConclusao());
        }

        private OrdemDeServico emExecucao() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            os.aprovar();
            return os;
        }
    }
}
