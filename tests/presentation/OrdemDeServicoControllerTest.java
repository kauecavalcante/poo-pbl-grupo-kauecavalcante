package presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemDeServicoControllerTest {

    private ServidorOficina servidor;
    private HttpClient http;
    private ObjectMapper mapper;
    private String base;

    @BeforeEach
    void iniciar() {
        servidor = new ServidorOficina("jdbc:sqlite::memory:").iniciar(0);
        http = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
        base = "http://localhost:" + servidor.porta();
    }

    @AfterEach
    void parar() {
        servidor.parar();
    }

    // ── POST /ordens ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens retorna 201 com id no corpo")
    void abrirRetorna201() throws Exception {
        Fixture f = novaFixture();
        HttpResponse<String> resp = post("/ordens",
            String.format("{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}", f.clienteId, f.veiculoId));
        assertEquals(201, resp.statusCode());
        JsonNode json = mapper.readTree(resp.body());
        assertTrue(json.has("id"));
        assertTrue(resp.headers().firstValue("Location").orElse("").startsWith("/ordens/"));
    }

    @Test
    @DisplayName("POST /ordens com cliente inexistente retorna 404")
    void abrirClienteInexistenteRetorna404() throws Exception {
        Fixture f = novaFixture();
        HttpResponse<String> resp = post("/ordens",
            String.format("{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}",
                java.util.UUID.randomUUID(), f.veiculoId));
        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("POST /ordens com veículo de outro cliente retorna 409")
    void abrirVeiculoDeOutroClienteRetorna409() throws Exception {
        Fixture f = novaFixture();
        String outroClienteId = cadastrarCliente("Joana", "111.444.777-35", "11988887777");
        HttpResponse<String> resp = post("/ordens",
            String.format("{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}",
                outroClienteId, f.veiculoId));
        assertEquals(409, resp.statusCode());
    }

    // ── GET /ordens/{id} ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /ordens/{id} retorna 200 com status RECEBIDA")
    void buscarRetorna200EmRecebida() throws Exception {
        Fixture f = novaFixture();
        String osId = abrirOrdem(f.clienteId, f.veiculoId);

        HttpResponse<String> resp = get("/ordens/" + osId);
        assertEquals(200, resp.statusCode());
        JsonNode json = mapper.readTree(resp.body());
        assertEquals("RECEBIDA", json.get("status").asText());
        assertEquals(f.clienteId, json.get("clienteId").asText());
        assertEquals(f.veiculoId, json.get("veiculoId").asText());
        assertTrue(json.get("diagnostico").isNull());
    }

    @Test
    @DisplayName("GET /ordens/{id} inexistente retorna 404")
    void buscarInexistenteRetorna404() throws Exception {
        HttpResponse<String> resp = get("/ordens/" + java.util.UUID.randomUUID());
        assertEquals(404, resp.statusCode());
    }

    // ── POST /ordens/{id}/diagnostico ────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/diagnostico retorna 204 e persiste texto")
    void diagnosticoRetorna204() throws Exception {
        Fixture f = novaFixture();
        String osId = abrirOrdem(f.clienteId, f.veiculoId);
        HttpResponse<String> resp = post("/ordens/" + osId + "/diagnostico",
            "{\"diagnostico\":\"Freios com ruído\"}");
        assertEquals(204, resp.statusCode());

        JsonNode os = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("Freios com ruído", os.get("diagnostico").asText());
    }

    @Test
    @DisplayName("POST /ordens/{id}/diagnostico em OS inexistente retorna 404")
    void diagnosticoOsInexistenteRetorna404() throws Exception {
        HttpResponse<String> resp = post("/ordens/" + java.util.UUID.randomUUID() + "/diagnostico",
            "{\"diagnostico\":\"Teste\"}");
        assertEquals(404, resp.statusCode());
    }

    // ── POST /ordens/{id}/orcamento ──────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/orcamento retorna 201 com id do orçamento")
    void orcamentoRetorna201() throws Exception {
        Fixture f = novaFixture();
        String osId = abrirOrdem(f.clienteId, f.veiculoId);
        post("/ordens/" + osId + "/diagnostico", "{\"diagnostico\":\"Diagnóstico\"}");

        String body = String.format("""
            {"pecas":[{"pecaId":"%s","quantidade":2}],
             "maosDeObra":[{"descricao":"Troca","precoReais":"100.00","quantidade":1}]}
            """, f.pecaId);
        HttpResponse<String> resp = post("/ordens/" + osId + "/orcamento", body);
        assertEquals(201, resp.statusCode());
        JsonNode json = mapper.readTree(resp.body());
        assertTrue(json.has("id"));
    }

    @Test
    @DisplayName("POST /ordens/{id}/orcamento com peça inexistente retorna 404")
    void orcamentoPecaInexistenteRetorna404() throws Exception {
        Fixture f = novaFixture();
        String osId = abrirOrdem(f.clienteId, f.veiculoId);
        post("/ordens/" + osId + "/diagnostico", "{\"diagnostico\":\"Diagnóstico\"}");

        String idFalso = java.util.UUID.randomUUID().toString();
        String body = String.format(
            "{\"pecas\":[{\"pecaId\":\"%s\",\"quantidade\":1}],\"maosDeObra\":[]}", idFalso);
        HttpResponse<String> resp = post("/ordens/" + osId + "/orcamento", body);
        assertEquals(404, resp.statusCode());
    }

    // ── POST /ordens/{id}/aprovar ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/aprovar retorna 204 e status vai para EM_EXECUCAO")
    void aprovarRetorna204() throws Exception {
        Fixture f = novaFixture();
        String osId = fluxoAteAguardandoAprovacao(f);

        HttpResponse<String> resp = post("/ordens/" + osId + "/aprovar", "");
        assertEquals(204, resp.statusCode());

        JsonNode os = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("EM_EXECUCAO", os.get("status").asText());
        assertFalse(os.get("orcamentoId").isNull());
    }

    @Test
    @DisplayName("POST /ordens/{id}/aprovar em estado incorreto retorna 409")
    void aprovarEstadoIncorretoRetorna409() throws Exception {
        Fixture f = novaFixture();
        String osId = abrirOrdem(f.clienteId, f.veiculoId);
        // OS está em RECEBIDA, não AGUARDANDO_APROVACAO
        HttpResponse<String> resp = post("/ordens/" + osId + "/aprovar", "");
        assertEquals(409, resp.statusCode());
    }

    // ── POST /ordens/{id}/rejeitar ────────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/rejeitar retorna 204 e preserva motivo")
    void rejeitarRetorna204() throws Exception {
        Fixture f = novaFixture();
        String osId = fluxoAteAguardandoAprovacao(f);

        HttpResponse<String> resp = post("/ordens/" + osId + "/rejeitar",
            "{\"motivo\":\"Preço acima do esperado\"}");
        assertEquals(204, resp.statusCode());

        JsonNode os = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("REJEITADA", os.get("status").asText());
        assertEquals("Preço acima do esperado", os.get("motivoRejeicao").asText());
    }

    // ── POST /ordens/{id}/concluir ────────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/concluir retorna 204 e status vai para CONCLUIDA")
    void concluirRetorna204() throws Exception {
        Fixture f = novaFixture();
        String osId = fluxoAteAguardandoAprovacao(f);
        post("/ordens/" + osId + "/aprovar", "");

        HttpResponse<String> resp = post("/ordens/" + osId + "/concluir", "");
        assertEquals(204, resp.statusCode());

        JsonNode os = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("CONCLUIDA", os.get("status").asText());
        assertFalse(os.get("dataConclusao").isNull());
    }

    // ── POST /ordens/{id}/entregar ────────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/entregar retorna 204 e status vai para ENTREGUE")
    void entregarRetorna204() throws Exception {
        Fixture f = novaFixture();
        String osId = fluxoAteAguardandoAprovacao(f);
        post("/ordens/" + osId + "/aprovar", "");
        post("/ordens/" + osId + "/concluir", "");

        HttpResponse<String> resp = post("/ordens/" + osId + "/entregar", "");
        assertEquals(204, resp.statusCode());

        JsonNode os = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("ENTREGUE", os.get("status").asText());
        assertFalse(os.get("dataEntrega").isNull());
    }

    // ── POST /ordens/{id}/cancelar ────────────────────────────────────────────

    @Test
    @DisplayName("POST /ordens/{id}/cancelar retorna 204 e preserva motivo")
    void cancelarRetorna204() throws Exception {
        Fixture f = novaFixture();
        String osId = abrirOrdem(f.clienteId, f.veiculoId);

        HttpResponse<String> resp = post("/ordens/" + osId + "/cancelar",
            "{\"motivo\":\"Cliente desistiu\"}");
        assertEquals(204, resp.statusCode());

        JsonNode os = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("CANCELADA", os.get("status").asText());
        assertEquals("Cliente desistiu", os.get("motivoCancelamento").asText());
    }

    // ── auxiliares ───────────────────────────────────────────────────────────

    private record Fixture(String clienteId, String veiculoId, String pecaId) {}

    private Fixture novaFixture() throws Exception {
        String clienteId = cadastrarCliente("Maria Silva", "529.982.247-25", "11912345678");
        String veiculoId = cadastrarVeiculo("ABC1234", "Fiat", "Uno", 2010, clienteId);
        String pecaId = cadastrarPeca("FLT-001", "Filtro de óleo", "50.00", 10);
        return new Fixture(clienteId, veiculoId, pecaId);
    }

    private String fluxoAteAguardandoAprovacao(Fixture f) throws Exception {
        String osId = abrirOrdem(f.clienteId, f.veiculoId);
        post("/ordens/" + osId + "/diagnostico", "{\"diagnostico\":\"Freios desgastados\"}");
        String body = String.format("""
            {"pecas":[{"pecaId":"%s","quantidade":1}],
             "maosDeObra":[{"descricao":"Troca","precoReais":"80.00","quantidade":1}]}
            """, f.pecaId);
        post("/ordens/" + osId + "/orcamento", body);
        return osId;
    }

    private String abrirOrdem(String clienteId, String veiculoId) throws Exception {
        String body = String.format(
            "{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}", clienteId, veiculoId);
        HttpResponse<String> resp = post("/ordens", body);
        return mapper.readTree(resp.body()).get("id").asText();
    }

    private String cadastrarCliente(String nome, String cpf, String tel) throws Exception {
        String body = String.format(
            "{\"nome\":\"%s\",\"cpf\":\"%s\",\"telefone\":\"%s\"}", nome, cpf, tel);
        return mapper.readTree(post("/clientes", body).body()).get("id").asText();
    }

    private String cadastrarVeiculo(String placa, String marca, String modelo,
                                    int ano, String donoId) throws Exception {
        String body = String.format(
            "{\"placa\":\"%s\",\"marca\":\"%s\",\"modelo\":\"%s\",\"ano\":%d,\"donoId\":\"%s\"}",
            placa, marca, modelo, ano, donoId);
        return mapper.readTree(post("/veiculos", body).body()).get("id").asText();
    }

    private String cadastrarPeca(String cod, String desc, String preco, int estoque) throws Exception {
        String body = String.format(
            "{\"codigo\":\"%s\",\"descricao\":\"%s\",\"precoReais\":\"%s\",\"estoqueInicial\":%d}",
            cod, desc, preco, estoque);
        return mapper.readTree(post("/pecas", body).body()).get("id").asText();
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(base + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(base + path))
            .GET()
            .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
