package presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class PecaControllerTest {

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
    @DisplayName("POST /pecas retorna 201 com id no corpo")
    void cadastrarRetorna201() throws Exception {
        String body = """
            {"codigo":"FLT-001","descricao":"Filtro de óleo","precoReais":"45.90","estoqueInicial":10}
            """;
        HttpResponse<String> resp = post("/pecas", body);
        assertEquals(201, resp.statusCode());
        JsonNode json = mapper.readTree(resp.body());
        assertTrue(json.has("id"));
        assertTrue(resp.headers().firstValue("Location").orElse("").startsWith("/pecas/"));
    }

    @Test
    @DisplayName("POST /pecas com código duplicado retorna 409")
    void cadastrarCodigoDuplicadoRetorna409() throws Exception {
        String body = """
            {"codigo":"FLT-001","descricao":"Filtro de óleo","precoReais":"45.90","estoqueInicial":10}
            """;
        post("/pecas", body);
        HttpResponse<String> resp = post("/pecas", body);
        assertEquals(409, resp.statusCode());
    }

    @Test
    @DisplayName("POST /pecas com preço inválido retorna 400")
    void cadastrarPrecoInvalidoRetorna400() throws Exception {
        String body = """
            {"codigo":"FLT-002","descricao":"Filtro","precoReais":"-10.00","estoqueInicial":5}
            """;
        HttpResponse<String> resp = post("/pecas", body);
        assertEquals(400, resp.statusCode());
    }

    @Test
    @DisplayName("GET /pecas/{id} retorna 200 com dados da peça")
    void buscarRetorna200() throws Exception {
        String body = """
            {"codigo":"FLT-001","descricao":"Filtro de óleo","precoReais":"45.90","estoqueInicial":10}
            """;
        HttpResponse<String> postResp = post("/pecas", body);
        String id = mapper.readTree(postResp.body()).get("id").asText();

        HttpResponse<String> getResp = get("/pecas/" + id);
        assertEquals(200, getResp.statusCode());
        JsonNode json = mapper.readTree(getResp.body());
        assertEquals("FLT-001", json.get("codigo").asText());
        assertEquals("Filtro de óleo", json.get("descricao").asText());
        assertEquals("45.90", json.get("precoReais").asText());
        assertEquals(10, json.get("estoque").asInt());
    }

    @Test
    @DisplayName("GET /pecas/{id} inexistente retorna 404")
    void buscarInexistenteRetorna404() throws Exception {
        HttpResponse<String> resp = get("/pecas/" + java.util.UUID.randomUUID());
        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("GET /pecas/{id-invalido} retorna 400")
    void buscarIdInvalidoRetorna400() throws Exception {
        HttpResponse<String> resp = get("/pecas/nao-e-uuid");
        assertEquals(400, resp.statusCode());
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
