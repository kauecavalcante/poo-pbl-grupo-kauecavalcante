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

class ClienteControllerTest {

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
    @DisplayName("POST /clientes retorna 201 com id no corpo")
    void cadastrarRetorna201() throws Exception {
        String body = """
            {"nome":"Maria Silva","cpf":"529.982.247-25","telefone":"11912345678"}
            """;
        HttpResponse<String> resp = post("/clientes", body);
        assertEquals(201, resp.statusCode());
        JsonNode json = mapper.readTree(resp.body());
        assertTrue(json.has("id"));
        assertTrue(resp.headers().firstValue("Location").orElse("").startsWith("/clientes/"));
    }

    @Test
    @DisplayName("POST /clientes com CPF duplicado retorna 409")
    void cadastrarCpfDuplicadoRetorna409() throws Exception {
        String body = """
            {"nome":"Maria Silva","cpf":"529.982.247-25","telefone":"11912345678"}
            """;
        post("/clientes", body);
        HttpResponse<String> resp = post("/clientes", body);
        assertEquals(409, resp.statusCode());
    }

    @Test
    @DisplayName("POST /clientes com CPF inválido retorna 400")
    void cadastrarCpfInvalidoRetorna400() throws Exception {
        String body = """
            {"nome":"Maria","cpf":"000.000.000-00","telefone":"11912345678"}
            """;
        HttpResponse<String> resp = post("/clientes", body);
        assertEquals(400, resp.statusCode());
    }

    @Test
    @DisplayName("GET /clientes/{id} retorna 200 com dados do cliente")
    void buscarRetorna200() throws Exception {
        String body = """
            {"nome":"Maria Silva","cpf":"529.982.247-25","telefone":"11912345678"}
            """;
        HttpResponse<String> postResp = post("/clientes", body);
        String id = mapper.readTree(postResp.body()).get("id").asText();

        HttpResponse<String> getResp = get("/clientes/" + id);
        assertEquals(200, getResp.statusCode());
        JsonNode json = mapper.readTree(getResp.body());
        assertEquals("Maria Silva", json.get("nome").asText());
        assertEquals(id, json.get("id").asText());
    }

    @Test
    @DisplayName("GET /clientes/{id} inexistente retorna 404")
    void buscarInexistenteRetorna404() throws Exception {
        HttpResponse<String> resp = get("/clientes/" + java.util.UUID.randomUUID());
        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("GET /clientes/{id-invalido} retorna 400")
    void buscarIdInvalidoRetorna400() throws Exception {
        HttpResponse<String> resp = get("/clientes/nao-e-uuid");
        assertEquals(400, resp.statusCode());
    }

    @Test
    @DisplayName("resposta de erro contém campos 'erro' e 'mensagem'")
    void erroTemCamposEsperados() throws Exception {
        HttpResponse<String> resp = get("/clientes/nao-e-uuid");
        JsonNode json = mapper.readTree(resp.body());
        assertTrue(json.has("erro"));
        assertTrue(json.has("mensagem"));
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
