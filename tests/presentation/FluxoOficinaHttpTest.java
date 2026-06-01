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

class FluxoOficinaHttpTest {

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

    @Test
    @DisplayName("fluxo completo HTTP: cadastros → diagnóstico → orçamento → aprovação → entrega")
    void fluxoCompletoAteEntrega() throws Exception {
        // 1. Cadastrar cliente
        String clienteId = idDe(post("/clientes",
            "{\"nome\":\"Maria Silva\",\"cpf\":\"529.982.247-25\",\"telefone\":\"11912345678\"}"));

        // 2. Cadastrar veículo
        String veiculoId = idDe(post("/veiculos",
            String.format("{\"placa\":\"ABC1234\",\"marca\":\"Fiat\",\"modelo\":\"Uno\",\"ano\":2010,\"donoId\":\"%s\"}",
                clienteId)));

        // 3. Cadastrar duas peças
        String filtroId = idDe(post("/pecas",
            "{\"codigo\":\"FLT-1001\",\"descricao\":\"Filtro de óleo\",\"precoReais\":\"50.00\",\"estoqueInicial\":10}"));
        String oleoId = idDe(post("/pecas",
            "{\"codigo\":\"OLEO-5W30\",\"descricao\":\"Óleo 5W30\",\"precoReais\":\"80.00\",\"estoqueInicial\":20}"));

        // 4. Abrir OS
        String osId = idDe(post("/ordens",
            String.format("{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}", clienteId, veiculoId)));

        // 5. Registrar diagnóstico
        assertEquals(204, post("/ordens/" + osId + "/diagnostico",
            "{\"diagnostico\":\"Suspensão dianteira com folga, troca de óleo\"}").statusCode());

        // 6. Montar orçamento — 50×1 + 80×4 + 120×1 = 490
        String orcamentoBody = String.format("""
            {"pecas":[{"pecaId":"%s","quantidade":1},{"pecaId":"%s","quantidade":4}],
             "maosDeObra":[{"descricao":"Troca de óleo e filtro","precoReais":"120.00","quantidade":1}]}
            """, filtroId, oleoId);
        HttpResponse<String> orcResp = post("/ordens/" + osId + "/orcamento", orcamentoBody);
        assertEquals(201, orcResp.statusCode());

        // 7. Verificar OS em AGUARDANDO_APROVACAO
        JsonNode osAguardando = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("AGUARDANDO_APROVACAO", osAguardando.get("status").asText());
        assertFalse(osAguardando.get("orcamentoId").isNull());

        // 8. Aprovar
        assertEquals(204, post("/ordens/" + osId + "/aprovar", "").statusCode());

        // 9. Concluir
        assertEquals(204, post("/ordens/" + osId + "/concluir", "").statusCode());

        // 10. Entregar
        assertEquals(204, post("/ordens/" + osId + "/entregar", "").statusCode());

        // 11. Verificar estado final
        JsonNode osFinal = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("ENTREGUE", osFinal.get("status").asText());
        assertEquals(clienteId, osFinal.get("clienteId").asText());
        assertEquals(veiculoId, osFinal.get("veiculoId").asText());
        assertFalse(osFinal.get("dataConclusao").isNull());
        assertFalse(osFinal.get("dataEntrega").isNull());
    }

    @Test
    @DisplayName("fluxo de rejeição HTTP preserva motivo e encerra com REJEITADA")
    void fluxoDeRejeicao() throws Exception {
        // Cadastros
        String clienteId = idDe(post("/clientes",
            "{\"nome\":\"João Souza\",\"cpf\":\"111.444.777-35\",\"telefone\":\"11999998888\"}"));
        String veiculoId = idDe(post("/veiculos",
            String.format("{\"placa\":\"XYZ1A23\",\"marca\":\"VW\",\"modelo\":\"Gol\",\"ano\":2018,\"donoId\":\"%s\"}",
                clienteId)));
        String pastilhaId = idDe(post("/pecas",
            "{\"codigo\":\"PST-2002\",\"descricao\":\"Pastilha de freio\",\"precoReais\":\"180.00\",\"estoqueInicial\":5}"));

        // Abrir OS e percorrer até orçamento
        String osId = idDe(post("/ordens",
            String.format("{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}", clienteId, veiculoId)));
        post("/ordens/" + osId + "/diagnostico", "{\"diagnostico\":\"Freios com ruído ao acionar\"}");
        String orcBody = String.format(
            "{\"pecas\":[{\"pecaId\":\"%s\",\"quantidade\":4}]," +
            "\"maosDeObra\":[{\"descricao\":\"Troca de pastilhas\",\"precoReais\":\"250.00\",\"quantidade\":1}]}",
            pastilhaId);
        post("/ordens/" + osId + "/orcamento", orcBody);

        // Rejeitar
        HttpResponse<String> rej = post("/ordens/" + osId + "/rejeitar",
            "{\"motivo\":\"Cliente preferiu segunda opinião\"}");
        assertEquals(204, rej.statusCode());

        // Verificar
        JsonNode osFinal = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("REJEITADA", osFinal.get("status").asText());
        assertEquals("Cliente preferiu segunda opinião", osFinal.get("motivoRejeicao").asText());
        assertTrue(osFinal.get("dataConclusao").isNull());
    }

    @Test
    @DisplayName("fluxo de cancelamento HTTP encerra com CANCELADA e motivo")
    void fluxoDeCancelamento() throws Exception {
        String clienteId = idDe(post("/clientes",
            "{\"nome\":\"Carlos Mota\",\"cpf\":\"987.654.321-00\",\"telefone\":\"11977776666\"}"));
        String veiculoId = idDe(post("/veiculos",
            String.format("{\"placa\":\"DEF5678\",\"marca\":\"Honda\",\"modelo\":\"Fit\",\"ano\":2016,\"donoId\":\"%s\"}",
                clienteId)));

        String osId = idDe(post("/ordens",
            String.format("{\"clienteId\":\"%s\",\"veiculoId\":\"%s\"}", clienteId, veiculoId)));

        HttpResponse<String> canc = post("/ordens/" + osId + "/cancelar",
            "{\"motivo\":\"Cliente levou para outra oficina\"}");
        assertEquals(204, canc.statusCode());

        JsonNode osFinal = mapper.readTree(get("/ordens/" + osId).body());
        assertEquals("CANCELADA", osFinal.get("status").asText());
        assertEquals("Cliente levou para outra oficina", osFinal.get("motivoCancelamento").asText());
    }

    // ── auxiliares ───────────────────────────────────────────────────────────

    private String idDe(HttpResponse<String> resp) throws Exception {
        return mapper.readTree(resp.body()).get("id").asText();
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
