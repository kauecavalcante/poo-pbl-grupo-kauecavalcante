package domain.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrcamentoTest {

    private static final PecaId PECA_ID = PecaId.novo();

    @Nested
    @DisplayName("Criação de orçamento vazio")
    class Criacao {

        @Test
        @DisplayName("novo gera identidade")
        void novoGeraIdentidade() {
            Orcamento o = Orcamento.novo();
            assertNotNull(o.id());
        }

        @Test
        @DisplayName("novo começa sem itens")
        void novoSemItens() {
            Orcamento o = Orcamento.novo();
            assertEquals(0, o.quantidadeDeItens());
            assertTrue(o.ehVazio());
        }

        @Test
        @DisplayName("total de orçamento vazio é Preco.zero")
        void totalVazioEhZero() {
            Orcamento o = Orcamento.novo();
            assertEquals(Preco.zero(), o.total());
        }

        @Test
        @DisplayName("dois orçamentos novos recebem IDs distintos")
        void novoGeraIdsDistintos() {
            assertEquals(false, Orcamento.novo().id().equals(Orcamento.novo().id()));
        }

        @Test
        @DisplayName("orçamento novo começa no estado RASCUNHO")
        void novoComecaEmRascunho() {
            assertEquals(StatusOrcamento.RASCUNHO, Orcamento.novo().status());
        }

        @Test
        @DisplayName("orçamento novo não tem motivo de rejeição")
        void novoNaoTemMotivoRejeicao() {
            assertEquals(Optional.empty(), Orcamento.novo().motivoRejeicao());
        }
    }

    @Nested
    @DisplayName("Inserção de itens")
    class Adicao {

        @Test
        @DisplayName("adicionarItemDePeca retorna ID não nulo do item criado")
        void adicionarItemDePecaRetornaId() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertNotNull(itemId);
        }

        @Test
        @DisplayName("adicionarItemDePeca incrementa quantidadeDeItens e remove ehVazio")
        void adicionarItemDePecaAtualizaContagem() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertEquals(1, o.quantidadeDeItens());
            assertFalse(o.ehVazio());
        }

        @Test
        @DisplayName("adicionarItemDePeca atualiza o total")
        void adicionarItemDePecaAtualizaTotal() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertEquals(Preco.deReais("100.00"), o.total());
        }

        @Test
        @DisplayName("adicionarItemDeMaoDeObra retorna ID e atualiza total")
        void adicionarItemDeMaoDeObra() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDeMaoDeObra("Troca de filtro", Preco.deReais("80.00"), 1);
            assertNotNull(itemId);
            assertEquals(Preco.deReais("80.00"), o.total());
        }

        @Test
        @DisplayName("misturar peças e mão de obra soma corretamente os subtotais")
        void misturarTiposSomaSubtotais() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            o.adicionarItemDeMaoDeObra("Troca de filtro", Preco.deReais("80.00"), 1);
            o.adicionarItemDePeca(PecaId.novo(), "Óleo", Preco.deReais("30.00"), 4);
            // 100 + 80 + 120 = 300
            assertEquals(Preco.deReais("300.00"), o.total());
            assertEquals(3, o.quantidadeDeItens());
        }

        @Test
        @DisplayName("cada item adicionado recebe um ID único")
        void cadaItemRecebeIdUnico() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId a = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            ItemDeOrcamentoId b = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            assertEquals(false, a.equals(b));
        }

        @Test
        @DisplayName("adicionarItemDePeca propaga validação do item (rejeita pecaId nulo)")
        void adicionarItemDePecaPropagaValidacao() {
            Orcamento o = Orcamento.novo();
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> o.adicionarItemDePeca(null, "Filtro", Preco.deReais("50.00"), 1)
            );
            assertEquals("item de peça exige pecaId", ex.getMessage());
        }

        @Test
        @DisplayName("adicionarItemDeMaoDeObra propaga validação do item (rejeita quantidade zero)")
        void adicionarItemDeMaoDeObraPropagaValidacao() {
            Orcamento o = Orcamento.novo();
            assertThrows(
                IllegalArgumentException.class,
                () -> o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 0)
            );
        }
    }

    @Nested
    @DisplayName("Encapsulamento da lista de itens")
    class Encapsulamento {

        @Test
        @DisplayName("itens retorna lista imutável — add lança UnsupportedOperationException")
        void itensImutavelAdd() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            List<ItemDeOrcamento> snapshot = o.itens();
            ItemDeOrcamento intruso = ItemDeOrcamento.deMaoDeObra("Intruso", Preco.deReais("1.00"), 1);
            assertThrows(UnsupportedOperationException.class, () -> snapshot.add(intruso));
        }

        @Test
        @DisplayName("itens retorna lista imutável — remove lança UnsupportedOperationException")
        void itensImutavelRemove() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            List<ItemDeOrcamento> snapshot = o.itens();
            assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));
        }

        @Test
        @DisplayName("snapshot obtido antes de adicionar não reflete alterações posteriores no agregado")
        void snapshotNaoReflectAlteracoesPosteriores() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            List<ItemDeOrcamento> snapshot = o.itens();
            assertEquals(1, snapshot.size());

            o.adicionarItemDeMaoDeObra("Mão de obra extra", Preco.deReais("80.00"), 1);
            assertEquals(1, snapshot.size());
            assertEquals(2, o.itens().size());
        }

        @Test
        @DisplayName("itens preserva a ordem de inserção e o conteúdo correto")
        void itensPreservaOrdemEConteudo() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId primeiro = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            ItemDeOrcamentoId segundo = o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            ItemDeOrcamentoId terceiro = o.adicionarItemDePeca(PecaId.novo(), "Óleo", Preco.deReais("30.00"), 2);

            List<ItemDeOrcamento> lista = o.itens();
            assertEquals(3, lista.size());
            assertEquals(primeiro, lista.get(0).id());
            assertEquals(segundo, lista.get(1).id());
            assertEquals(terceiro, lista.get(2).id());
        }
    }

    @Nested
    @DisplayName("Remoção e atualização de itens")
    class RemocaoEAtualizacao {

        @Test
        @DisplayName("removerItem retira o item da lista e ajusta o total")
        void removerItem() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemA = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 1);

            o.removerItem(itemA);

            assertEquals(1, o.quantidadeDeItens());
            assertEquals(Preco.deReais("80.00"), o.total());
        }

        @Test
        @DisplayName("removerItem rejeita ID não pertencente ao orçamento")
        void removerItemInexistente() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> o.removerItem(ItemDeOrcamentoId.novo())
            );
            assertEquals("item não encontrado no orçamento", ex.getMessage());
        }

        @Test
        @DisplayName("removerItem rejeita ID nulo")
        void removerItemRejeitaIdNulo() {
            Orcamento o = Orcamento.novo();
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> o.removerItem(null)
            );
            assertEquals("id do item de orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("atualizarQuantidadeDeItem atualiza quantidade e total")
        void atualizarQuantidadeDeItem() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);

            o.atualizarQuantidadeDeItem(itemId, 5);

            assertEquals(Preco.deReais("250.00"), o.total());
        }

        @Test
        @DisplayName("atualizarQuantidadeDeItem rejeita ID não pertencente ao orçamento")
        void atualizarQuantidadeDeItemInexistente() {
            Orcamento o = Orcamento.novo();
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> o.atualizarQuantidadeDeItem(ItemDeOrcamentoId.novo(), 3)
            );
            assertEquals("item não encontrado no orçamento", ex.getMessage());
        }

        @Test
        @DisplayName("atualizarQuantidadeDeItem rejeita ID nulo")
        void atualizarQuantidadeDeItemRejeitaIdNulo() {
            Orcamento o = Orcamento.novo();
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> o.atualizarQuantidadeDeItem(null, 3)
            );
            assertEquals("id do item de orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("atualizarQuantidadeDeItem rejeita quantidade não positiva")
        void atualizarQuantidadeDeItemRejeitaNaoPositiva() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertThrows(
                IllegalArgumentException.class,
                () -> o.atualizarQuantidadeDeItem(itemId, 0)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> o.atualizarQuantidadeDeItem(itemId, -1)
            );
        }

        @Test
        @DisplayName("remover todos os itens deixa o orçamento vazio novamente")
        void removerTodosOsItens() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId a = o.adicionarItemDeMaoDeObra("Troca A", Preco.deReais("10.00"), 1);
            ItemDeOrcamentoId b = o.adicionarItemDeMaoDeObra("Troca B", Preco.deReais("20.00"), 1);
            o.removerItem(a);
            o.removerItem(b);
            assertTrue(o.ehVazio());
            assertEquals(Preco.zero(), o.total());
        }
    }

    @Nested
    @DisplayName("Reconstituição a partir de identidade existente")
    class Reconstituicao {

        @Test
        @DisplayName("reconstituir preserva ID e itens")
        void reconstituirPreservaIdEItens() {
            OrcamentoId id = OrcamentoId.novo();
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            Orcamento o = Orcamento.reconstituir(id, List.of(item), new Rascunho());
            assertEquals(id, o.id());
            assertEquals(1, o.quantidadeDeItens());
            assertEquals(item, o.itens().get(0));
            assertEquals(Preco.deReais("80.00"), o.total());
        }

        @Test
        @DisplayName("reconstituir aceita lista vazia")
        void reconstituirComListaVazia() {
            Orcamento o = Orcamento.reconstituir(OrcamentoId.novo(), List.of(), new Rascunho());
            assertTrue(o.ehVazio());
        }

        @Test
        @DisplayName("reconstituir rejeita ID nulo")
        void reconstituirRejeitaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(null, List.of(), new Rascunho())
            );
            assertEquals("id do orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir rejeita lista nula")
        void reconstituirRejeitaListaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(OrcamentoId.novo(), null, new Rascunho())
            );
            assertEquals("lista de itens não pode ser nula", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir rejeita itens nulos dentro da lista")
        void reconstituirRejeitaItensNulosNaLista() {
            ItemDeOrcamento valido = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(OrcamentoId.novo(), Arrays.asList(valido, null), new Rascunho())
            );
            assertEquals("itens do orçamento não podem ser nulos", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir faz cópia defensiva — mutação posterior da lista não afeta o agregado")
        void reconstituirFazCopiaDefensiva() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            List<ItemDeOrcamento> entrada = new ArrayList<>();
            entrada.add(item);
            Orcamento o = Orcamento.reconstituir(OrcamentoId.novo(), entrada, new Rascunho());

            entrada.clear();

            assertEquals(1, o.quantidadeDeItens());
        }

        @Test
        @DisplayName("reconstituir rejeita estado nulo")
        void reconstituirRejeitaEstadoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(OrcamentoId.novo(), List.of(), null)
            );
            assertEquals("estado do orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir preserva o estado informado (Aprovado)")
        void reconstituirPreservaEstadoAprovado() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            Orcamento o = Orcamento.reconstituir(OrcamentoId.novo(), List.of(item), new Aprovado());
            assertEquals(StatusOrcamento.APROVADO, o.status());
        }

        @Test
        @DisplayName("reconstituir em estado terminal bloqueia edição de itens")
        void reconstituirEmAprovadoBloqueiaEdicao() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            Orcamento o = Orcamento.reconstituir(OrcamentoId.novo(), List.of(item), new Aprovado());
            assertThrows(
                IllegalStateException.class,
                () -> o.adicionarItemDeMaoDeObra("Intruso", Preco.deReais("1.00"), 1)
            );
        }

        @Test
        @DisplayName("reconstituir em Rejeitado preserva motivo de rejeição")
        void reconstituirEmRejeitadoPreservaMotivo() {
            Orcamento o = Orcamento.reconstituir(
                OrcamentoId.novo(), List.of(), new Rejeitado("Cliente desistiu")
            );
            assertEquals(StatusOrcamento.REJEITADO, o.status());
            assertEquals(Optional.of("Cliente desistiu"), o.motivoRejeicao());
        }
    }

    @Nested
    @DisplayName("Igualdade por identidade")
    class Igualdade {

        @Test
        @DisplayName("dois orçamentos com mesmo ID são iguais mesmo com itens divergentes")
        void iguaisQuandoMesmoIdAindaQueItensDivirjam() {
            OrcamentoId id = OrcamentoId.novo();
            Orcamento a = Orcamento.reconstituir(id, List.of(
                ItemDeOrcamento.deMaoDeObra("Troca A", Preco.deReais("10.00"), 1)
            ), new Rascunho());
            Orcamento b = Orcamento.reconstituir(id, List.of(
                ItemDeOrcamento.deMaoDeObra("Troca B", Preco.deReais("999.00"), 5)
            ), new Rascunho());
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("orçamentos com IDs diferentes não são iguais")
        void distintosQuandoIdsDiferentes() {
            assertNotEquals(Orcamento.novo(), Orcamento.novo());
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(Orcamento.novo().equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Orcamento o = Orcamento.novo();
            assertFalse(o.equals(o.id()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Orcamento o = Orcamento.novo();
            assertTrue(o.equals(o));
        }
    }

    @Nested
    @DisplayName("Transições de estado")
    class Transicoes {

        @Test
        @DisplayName("fluxo feliz: novo → adicionar item → enviar → aprovar")
        void fluxoFelizCompleto() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.enviar();
            assertEquals(StatusOrcamento.ENVIADO, o.status());
            o.aprovar();
            assertEquals(StatusOrcamento.APROVADO, o.status());
        }

        @Test
        @DisplayName("fluxo de rejeição: enviar → rejeitar com motivo")
        void fluxoRejeicao() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.enviar();
            o.rejeitar("Cliente desistiu da compra");
            assertEquals(StatusOrcamento.REJEITADO, o.status());
            assertEquals(Optional.of("Cliente desistiu da compra"), o.motivoRejeicao());
        }

        @Test
        @DisplayName("enviar de orçamento vazio é bloqueado")
        void enviarVazioBloqueado() {
            Orcamento o = Orcamento.novo();
            IllegalStateException ex = assertThrows(IllegalStateException.class, o::enviar);
            assertEquals("orçamento sem itens não pode ser enviado", ex.getMessage());
        }

        @Test
        @DisplayName("aprovar em rascunho é bloqueado")
        void aprovarEmRascunhoBloqueado() {
            Orcamento o = Orcamento.novo();
            IllegalStateException ex = assertThrows(IllegalStateException.class, o::aprovar);
            assertEquals("orçamento em rascunho não pode ser aprovado", ex.getMessage());
        }

        @Test
        @DisplayName("rejeitar em rascunho é bloqueado")
        void rejeitarEmRascunhoBloqueado() {
            Orcamento o = Orcamento.novo();
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.rejeitar("motivo qualquer")
            );
            assertEquals("orçamento em rascunho não pode ser rejeitado", ex.getMessage());
        }

        @Test
        @DisplayName("reenviar é bloqueado em Enviado")
        void reenviarBloqueado() {
            Orcamento o = orcamentoEnviado();
            assertThrows(IllegalStateException.class, o::enviar);
        }

        @Test
        @DisplayName("aprovar duas vezes é bloqueado")
        void aprovarDuasVezesBloqueado() {
            Orcamento o = orcamentoEnviado();
            o.aprovar();
            assertThrows(IllegalStateException.class, o::aprovar);
        }

        @Test
        @DisplayName("rejeitar após aprovar é bloqueado")
        void rejeitarAposAprovarBloqueado() {
            Orcamento o = orcamentoEnviado();
            o.aprovar();
            assertThrows(IllegalStateException.class, () -> o.rejeitar("motivo"));
        }

        @Test
        @DisplayName("aprovar após rejeitar é bloqueado")
        void aprovarAposRejeitarBloqueado() {
            Orcamento o = orcamentoEnviado();
            o.rejeitar("Cliente desistiu");
            assertThrows(IllegalStateException.class, o::aprovar);
        }

        @Test
        @DisplayName("rejeitar com motivo nulo lança IllegalArgumentException")
        void rejeitarComMotivoNulo() {
            Orcamento o = orcamentoEnviado();
            assertThrows(IllegalArgumentException.class, () -> o.rejeitar(null));
        }

        @Test
        @DisplayName("rejeitar com motivo curto lança IllegalArgumentException")
        void rejeitarComMotivoCurto() {
            Orcamento o = orcamentoEnviado();
            assertThrows(IllegalArgumentException.class, () -> o.rejeitar("AB"));
        }

        @Test
        @DisplayName("motivoRejeicao permanece Optional.empty fora de Rejeitado")
        void motivoRejeicaoVazioForaDeRejeitado() {
            Orcamento o = orcamentoEnviado();
            assertEquals(Optional.empty(), o.motivoRejeicao());
            o.aprovar();
            assertEquals(Optional.empty(), o.motivoRejeicao());
        }

        private Orcamento orcamentoEnviado() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.enviar();
            return o;
        }
    }

    @Nested
    @DisplayName("Restrição de edição por estado")
    class RestricaoDeEdicao {

        @Test
        @DisplayName("adicionarItemDePeca em ENVIADO é bloqueado")
        void adicionarItemDePecaEmEnviadoBloqueado() {
            Orcamento o = orcamentoEnviado();
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.adicionarItemDePeca(PECA_ID, "Outra peça", Preco.deReais("30.00"), 1)
            );
            assertTrue(ex.getMessage().contains("ENVIADO"));
        }

        @Test
        @DisplayName("adicionarItemDeMaoDeObra em ENVIADO é bloqueado")
        void adicionarItemDeMaoDeObraEmEnviadoBloqueado() {
            Orcamento o = orcamentoEnviado();
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.adicionarItemDeMaoDeObra("Diagnóstico", Preco.deReais("50.00"), 1)
            );
            assertTrue(ex.getMessage().contains("ENVIADO"));
        }

        @Test
        @DisplayName("removerItem em ENVIADO é bloqueado")
        void removerItemEmEnviadoBloqueado() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.enviar();
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.removerItem(itemId)
            );
            assertTrue(ex.getMessage().contains("ENVIADO"));
        }

        @Test
        @DisplayName("atualizarQuantidadeDeItem em ENVIADO é bloqueado")
        void atualizarQuantidadeEmEnviadoBloqueado() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.enviar();
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.atualizarQuantidadeDeItem(itemId, 5)
            );
            assertTrue(ex.getMessage().contains("ENVIADO"));
        }

        @Test
        @DisplayName("edição em APROVADO menciona o estado correto na mensagem")
        void edicaoEmAprovadoBloqueada() {
            Orcamento o = orcamentoEnviado();
            o.aprovar();
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.adicionarItemDeMaoDeObra("Diagnóstico", Preco.deReais("50.00"), 1)
            );
            assertTrue(ex.getMessage().contains("APROVADO"));
        }

        @Test
        @DisplayName("edição em REJEITADO menciona o estado correto na mensagem")
        void edicaoEmRejeitadoBloqueada() {
            Orcamento o = orcamentoEnviado();
            o.rejeitar("Cliente desistiu");
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> o.adicionarItemDeMaoDeObra("Diagnóstico", Preco.deReais("50.00"), 1)
            );
            assertTrue(ex.getMessage().contains("REJEITADO"));
        }

        @Test
        @DisplayName("edição continua livre em RASCUNHO (regressão)")
        void edicaoEmRascunhoFunciona() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            assertEquals(2, o.quantidadeDeItens());
        }

        private Orcamento orcamentoEnviado() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            o.enviar();
            return o;
        }
    }

    @Nested
    @DisplayName("Representação textual")
    class Formatacao {

        @Test
        @DisplayName("toString contém o ID")
        void toStringContemId() {
            Orcamento o = Orcamento.novo();
            assertTrue(o.toString().contains(o.id().toString()));
        }

        @Test
        @DisplayName("toString contém a quantidade de itens")
        void toStringContemQuantidadeDeItens() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            o.adicionarItemDeMaoDeObra("Outra", Preco.deReais("40.00"), 1);
            assertTrue(o.toString().contains("2"));
        }

        @Test
        @DisplayName("toString contém o total formatado")
        void toStringContemTotal() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            assertTrue(o.toString().contains(o.total().toString()));
        }
    }
}
