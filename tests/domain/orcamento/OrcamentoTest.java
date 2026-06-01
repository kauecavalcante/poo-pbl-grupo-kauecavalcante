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
            Orcamento o = Orcamento.reconstituir(id, List.of(item));
            assertEquals(id, o.id());
            assertEquals(1, o.quantidadeDeItens());
            assertEquals(item, o.itens().get(0));
            assertEquals(Preco.deReais("80.00"), o.total());
        }

        @Test
        @DisplayName("reconstituir aceita lista vazia")
        void reconstituirComListaVazia() {
            Orcamento o = Orcamento.reconstituir(OrcamentoId.novo(), List.of());
            assertTrue(o.ehVazio());
        }

        @Test
        @DisplayName("reconstituir rejeita ID nulo")
        void reconstituirRejeitaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(null, List.of())
            );
            assertEquals("id do orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir rejeita lista nula")
        void reconstituirRejeitaListaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(OrcamentoId.novo(), null)
            );
            assertEquals("lista de itens não pode ser nula", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir rejeita itens nulos dentro da lista")
        void reconstituirRejeitaItensNulosNaLista() {
            ItemDeOrcamento valido = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Orcamento.reconstituir(OrcamentoId.novo(), Arrays.asList(valido, null))
            );
            assertEquals("itens do orçamento não podem ser nulos", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir faz cópia defensiva — mutação posterior da lista não afeta o agregado")
        void reconstituirFazCopiaDefensiva() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
            List<ItemDeOrcamento> entrada = new ArrayList<>();
            entrada.add(item);
            Orcamento o = Orcamento.reconstituir(OrcamentoId.novo(), entrada);

            entrada.clear();

            assertEquals(1, o.quantidadeDeItens());
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
            ));
            Orcamento b = Orcamento.reconstituir(id, List.of(
                ItemDeOrcamento.deMaoDeObra("Troca B", Preco.deReais("999.00"), 5)
            ));
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
}
