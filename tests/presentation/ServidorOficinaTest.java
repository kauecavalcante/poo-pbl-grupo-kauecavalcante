package presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServidorOficinaTest {

    private ServidorOficina servidor;
    private HttpClient http;
    private String base;

    @BeforeEach
    void iniciar() {
        servidor = new ServidorOficina("jdbc:sqlite::memory:").iniciar(0);
        http = HttpClient.newHttpClient();
        base = "http://localhost:" + servidor.porta();
    }

    @AfterEach
    void parar() {
        servidor.parar();
    }

    @Test
    @DisplayName("servidor inicia e responde a rota inexistente com 404")
    void rotaInexistenteRetorna404() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(base + "/nao-existe"))
            .GET()
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("servidor retorna Content-Type application/json em erro")
    void erroRetornaJson() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(base + "/clientes/id-invalido"))
            .GET()
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, resp.statusCode());
        assertTrue(resp.headers().firstValue("Content-Type")
            .orElse("").contains("application/json"));
    }

    @Test
    @DisplayName("GET /clientes/{uuid-inexistente} retorna 404")
    void clienteInexistenteRetorna404() throws Exception {
        String uuid = java.util.UUID.randomUUID().toString();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(base + "/clientes/" + uuid))
            .GET()
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }
}
