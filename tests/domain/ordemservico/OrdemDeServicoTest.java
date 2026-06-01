package domain.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Nested
    @DisplayName("Cancelamento")
    class Cancelamento {

        @Test
        @DisplayName("cancelar em RECEBIDA transita para CANCELADA e expõe motivo")
        void cancelarEmRecebida() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.cancelar(MOTIVO);
            assertEquals(StatusOS.CANCELADA, os.status());
            assertEquals(Optional.of(MOTIVO), os.motivoCancelamento());
        }

        @Test
        @DisplayName("cancelar em AGUARDANDO_APROVACAO transita para CANCELADA")
        void cancelarEmAguardando() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            os.cancelar(MOTIVO);
            assertEquals(StatusOS.CANCELADA, os.status());
        }

        @Test
        @DisplayName("cancelar em EM_EXECUCAO transita para CANCELADA")
        void cancelarEmExecucao() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            os.aprovar();
            os.cancelar(MOTIVO);
            assertEquals(StatusOS.CANCELADA, os.status());
        }

        @Test
        @DisplayName("cancelar em CONCLUIDA é permitido")
        void cancelarEmConcluida() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            os.aprovar();
            os.concluir();
            os.cancelar(MOTIVO);
            assertEquals(StatusOS.CANCELADA, os.status());
        }

        @Test
        @DisplayName("cancelar em ENTREGUE é bloqueado (terminal)")
        void cancelarEmEntregueBloqueado() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            os.aprovar();
            os.concluir();
            os.entregar();
            assertThrows(IllegalStateException.class, () -> os.cancelar(MOTIVO));
        }

        @Test
        @DisplayName("cancelar em REJEITADA é bloqueado (terminal)")
        void cancelarEmRejeitadaBloqueado() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            os.rejeitar(MOTIVO);
            assertThrows(IllegalStateException.class, () -> os.cancelar("outro motivo"));
        }

        @Test
        @DisplayName("cancelar em CANCELADA é bloqueado (terminal)")
        void cancelarEmCanceladaBloqueado() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.cancelar(MOTIVO);
            assertThrows(IllegalStateException.class, () -> os.cancelar("outro motivo"));
        }

        @Test
        @DisplayName("cancelar com motivo inválido lança IllegalArgumentException")
        void cancelarComMotivoInvalido() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertThrows(IllegalArgumentException.class, () -> os.cancelar(null));
            assertThrows(IllegalArgumentException.class, () -> os.cancelar("AB"));
        }

        @Test
        @DisplayName("motivoCancelamento permanece vazio fora de CANCELADA")
        void motivoCancelamentoVazioForaDeCancelada() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(Optional.empty(), os.motivoCancelamento());
        }
    }

    @Nested
    @DisplayName("Reconstituição a partir de identidade existente")
    class Reconstituicao {

        private final OrdemDeServicoId ID = OrdemDeServicoId.novo();
        private final LocalDate ABERTURA = LocalDate.of(2024, 1, 10);

        @Test
        @DisplayName("reconstituir preserva todos os campos informados")
        void reconstituirPreservaCampos() {
            LocalDate conclusao = LocalDate.of(2024, 1, 15);
            LocalDate entrega = LocalDate.of(2024, 1, 16);
            OrdemDeServico os = OrdemDeServico.reconstituir(
                ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                new Entregue(), ABERTURA, conclusao, entrega
            );
            assertEquals(ID, os.id());
            assertSame(CLIENTE_ID, os.clienteId());
            assertSame(VEICULO_ID, os.veiculoId());
            assertEquals(Optional.of(ORCAMENTO_ID), os.orcamentoId());
            assertEquals(Optional.of(DIAGNOSTICO), os.diagnostico());
            assertEquals(StatusOS.ENTREGUE, os.status());
            assertEquals(ABERTURA, os.dataAbertura());
            assertEquals(Optional.of(conclusao), os.dataConclusao());
            assertEquals(Optional.of(entrega), os.dataEntrega());
        }

        @Test
        @DisplayName("reconstituir aceita OS em RECEBIDA sem datas finais")
        void reconstituirRecebida() {
            OrdemDeServico os = OrdemDeServico.reconstituir(
                ID, CLIENTE_ID, VEICULO_ID, null, null,
                new Recebida(), ABERTURA, null, null
            );
            assertEquals(StatusOS.RECEBIDA, os.status());
            assertEquals(Optional.empty(), os.dataConclusao());
        }

        @Test
        @DisplayName("reconstituir rejeita id nulo")
        void rejeitaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(null, CLIENTE_ID, VEICULO_ID, null, null,
                    new Recebida(), ABERTURA, null, null)
            );
            assertEquals("id da ordem de serviço não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir rejeita estado nulo")
        void rejeitaEstadoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, null, null,
                    null, ABERTURA, null, null)
            );
            assertEquals("estado da OS não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir rejeita data de abertura nula")
        void rejeitaDataAberturaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, null, null,
                    new Recebida(), null, null, null)
            );
            assertEquals("data de abertura não pode ser nula", ex.getMessage());
        }

        @Test
        @DisplayName("ENTREGUE exige dataConclusao presente")
        void entregueExigeDataConclusao() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new Entregue(), ABERTURA, null, LocalDate.of(2024, 1, 16))
            );
            assertEquals("OS no estado ENTREGUE precisa ter dataConclusao", ex.getMessage());
        }

        @Test
        @DisplayName("ENTREGUE exige dataEntrega presente")
        void entregueExigeDataEntrega() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new Entregue(), ABERTURA, LocalDate.of(2024, 1, 15), null)
            );
            assertEquals("OS no estado ENTREGUE precisa ter dataEntrega", ex.getMessage());
        }

        @Test
        @DisplayName("CONCLUIDA exige dataConclusao presente")
        void concluidaExigeDataConclusao() {
            assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new Concluida(), ABERTURA, null, null)
            );
        }

        @Test
        @DisplayName("CONCLUIDA não pode ter dataEntrega")
        void concluidaNaoPodeTerDataEntrega() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new Concluida(), ABERTURA, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16))
            );
            assertEquals("OS no estado CONCLUIDA não pode ter dataEntrega", ex.getMessage());
        }

        @Test
        @DisplayName("RECEBIDA não pode ter dataConclusao")
        void recebidaNaoPodeTerDataConclusao() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, null, null,
                    new Recebida(), ABERTURA, LocalDate.of(2024, 1, 15), null)
            );
            assertEquals("OS no estado RECEBIDA não pode ter dataConclusao", ex.getMessage());
        }

        @Test
        @DisplayName("EM_EXECUCAO não pode ter dataConclusao nem dataEntrega")
        void emExecucaoSemDatasFinais() {
            assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new EmExecucao(), ABERTURA, LocalDate.now(), null)
            );
        }

        @Test
        @DisplayName("REJEITADA não pode ter dataConclusao nem dataEntrega")
        void rejeitadaSemDatasFinais() {
            assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new Rejeitada(MOTIVO), ABERTURA, LocalDate.now(), null)
            );
        }

        @Test
        @DisplayName("CANCELADA aceita dataConclusao presente (cancelamento após conclusão)")
        void canceladaAceitaDataConclusao() {
            OrdemDeServico os = OrdemDeServico.reconstituir(
                ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                new Cancelada(MOTIVO), ABERTURA, LocalDate.of(2024, 1, 15), null
            );
            assertEquals(StatusOS.CANCELADA, os.status());
            assertEquals(Optional.of(LocalDate.of(2024, 1, 15)), os.dataConclusao());
        }

        @Test
        @DisplayName("CANCELADA não pode ter dataEntrega")
        void canceladaNaoPodeTerDataEntrega() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.reconstituir(ID, CLIENTE_ID, VEICULO_ID, ORCAMENTO_ID, DIAGNOSTICO,
                    new Cancelada(MOTIVO), ABERTURA, null, LocalDate.now())
            );
            assertEquals("OS no estado CANCELADA não pode ter dataEntrega", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Igualdade por identidade")
    class Igualdade {

        @Test
        @DisplayName("duas OS com mesmo ID são iguais mesmo com dados divergentes")
        void iguaisQuandoMesmoIdAindaQueDadosMudem() {
            OrdemDeServicoId id = OrdemDeServicoId.novo();
            OrdemDeServico a = OrdemDeServico.reconstituir(
                id, CLIENTE_ID, VEICULO_ID, null, null,
                new Recebida(), LocalDate.of(2024, 1, 10), null, null
            );
            OrdemDeServico b = OrdemDeServico.reconstituir(
                id, ClienteId.novo(), VeiculoId.novo(), ORCAMENTO_ID, DIAGNOSTICO,
                new EmExecucao(), LocalDate.of(2024, 2, 20), null, null
            );
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("OS com IDs diferentes não são iguais")
        void distintasQuandoIdsDiferentes() {
            assertNotEquals(
                OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID),
                OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID)
            );
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID).equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertFalse(os.equals(os.id()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertTrue(os.equals(os));
        }
    }

    @Nested
    @DisplayName("Representação textual")
    class Formatacao {

        @Test
        @DisplayName("toString contém id, status, clienteId, veiculoId e dataAbertura")
        void toStringContemCamposBasicos() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            String texto = os.toString();
            assertTrue(texto.contains(os.id().toString()));
            assertTrue(texto.contains("RECEBIDA"));
            assertTrue(texto.contains(CLIENTE_ID.toString()));
            assertTrue(texto.contains(VEICULO_ID.toString()));
            assertTrue(texto.contains(os.dataAbertura().toString()));
        }

        @Test
        @DisplayName("toString não inclui orcamentoId quando ainda não anexado")
        void toStringSemOrcamento() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertFalse(os.toString().contains("orcamentoId"));
        }

        @Test
        @DisplayName("toString inclui orcamentoId após anexação")
        void toStringComOrcamento() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico(DIAGNOSTICO);
            os.anexarOrcamento(ORCAMENTO_ID);
            assertTrue(os.toString().contains(ORCAMENTO_ID.toString()));
        }

        @Test
        @DisplayName("toString reflete o estado atual após transição")
        void toStringReflexaTransicao() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.cancelar(MOTIVO);
            assertTrue(os.toString().contains("CANCELADA"));
        }
    }
}
