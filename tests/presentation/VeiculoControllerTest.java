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

class VeiculoControllerTest {

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
    @DisplayName("POST /veiculos retorna 201 com id no corpo")
    void cadastrarRetorna201() throws Exception {
        String clienteId = cadastrarCliente("Ana Lima", "529.982.247-25", "11911111111");
        String body = String.format(
            "{\"placa\":\"ABC1234\",\"marca\":\"Fiat\",\"modelo\":\"Uno\",\"ano\":2010,\"donoId\":\"%s\"}",
            clienteId
        );
        HttpResponse<String> resp = post("/veiculos", body);
        assertEquals(201, resp.statusCode());
        JsonNode json = mapper.readTree(resp.body());
        assertTrue(json.has("id"));
        assertTrue(resp.headers().firstValue("Location").orElse("").startsWith("/veiculos/"));
    }

    @Test
    @DisplayName("POST /veiculos com placa duplicada retorna 409")
    void cadastrarPlacaDuplicadaRetorna409() throws Exception {
        String clienteId = cadastrarCliente("Ana Lima", "529.982.247-25", "11911111111");
        String body = String.format(
            "{\"placa\":\"ABC1234\",\"marca\":\"Fiat\",\"modelo\":\"Uno\",\"ano\":2010,\"donoId\":\"%s\"}",
            clienteId
        );
        post("/veiculos", body);
        HttpResponse<String> resp = post("/veiculos", body);
        assertEquals(409, resp.statusCode());
    }

    @Test
    @DisplayName("POST /veiculos com cliente inexistente retorna 404")
    void cadastrarClienteInexistenteRetorna404() throws Exception {
        String idFalso = java.util.UUID.randomUUID().toString();
        String body = String.format(
            "{\"placa\":\"XYZ9999\",\"marca\":\"VW\",\"modelo\":\"Gol\",\"ano\":2015,\"donoId\":\"%s\"}",
            idFalso
        );
        HttpResponse<String> resp = post("/veiculos", body);
        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("GET /veiculos/{id} retorna 200 com dados do veículo")
    void buscarRetorna200() throws Exception {
        String clienteId = cadastrarCliente("Ana Lima", "529.982.247-25", "11911111111");
        String body = String.format(
            "{\"placa\":\"ABC1234\",\"marca\":\"Fiat\",\"modelo\":\"Uno\",\"ano\":2010,\"donoId\":\"%s\"}",
            clienteId
        );
        HttpResponse<String> postResp = post("/veiculos", body);
        String id = mapper.readTree(postResp.body()).get("id").asText();

        HttpResponse<String> getResp = get("/veiculos/" + id);
        assertEquals(200, getResp.statusCode());
        JsonNode json = mapper.readTree(getResp.body());
        assertEquals("Fiat", json.get("marca").asText());
        assertEquals("Uno", json.get("modelo").asText());
        assertEquals(2010, json.get("ano").asInt());
    }

    @Test
    @DisplayName("GET /veiculos/{id} inexistente retorna 404")
    void buscarInexistenteRetorna404() throws Exception {
        HttpResponse<String> resp = get("/veiculos/" + java.util.UUID.randomUUID());
        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("GET /veiculos/{id-invalido} retorna 400")
    void buscarIdInvalidoRetorna400() throws Exception {
        HttpResponse<String> resp = get("/veiculos/nao-e-uuid");
        assertEquals(400, resp.statusCode());
    }

    // ── auxiliares ──────────────────────────────────────────────────────────

    private String cadastrarCliente(String nome, String cpf, String telefone) throws Exception {
        String body = String.format(
            "{\"nome\":\"%s\",\"cpf\":\"%s\",\"telefone\":\"%s\"}",
            nome, cpf, telefone
        );
        HttpResponse<String> resp = post("/clientes", body);
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
