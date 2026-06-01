package presentation;

import application.cliente.CadastrarCliente;
import application.ordemservico.AbrirOrdemDeServico;
import application.ordemservico.AprovarOrcamentoDaOS;
import application.ordemservico.CancelarOrdemDeServico;
import application.ordemservico.ConcluirServico;
import application.ordemservico.EntregarVeiculo;
import application.ordemservico.MontarOrcamentoDaOS;
import application.ordemservico.RegistrarDiagnosticoNaOS;
import application.ordemservico.RejeitarOrcamentoDaOS;
import application.peca.CadastrarPeca;
import application.veiculo.CadastrarVeiculo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import infrastructure.sqlite.ClienteSqliteRepository;
import infrastructure.sqlite.OficinaDatabase;
import infrastructure.sqlite.OrcamentoSqliteRepository;
import infrastructure.sqlite.OrdemDeServicoSqliteRepository;
import infrastructure.sqlite.PecaSqliteRepository;
import infrastructure.sqlite.VeiculoSqliteRepository;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.http.staticfiles.Location;
import presentation.controller.ClienteController;
import presentation.controller.OrdemDeServicoController;
import presentation.controller.PecaController;
import presentation.controller.VeiculoController;

public final class ServidorOficina {

    private final Javalin app;
    private final OficinaDatabase database;

    public ServidorOficina(String jdbcUrl) {
        this.database = new OficinaDatabase(jdbcUrl);

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Repositórios
        ClienteSqliteRepository clienteRepo = new ClienteSqliteRepository(database);
        VeiculoSqliteRepository veiculoRepo = new VeiculoSqliteRepository(database);
        PecaSqliteRepository pecaRepo = new PecaSqliteRepository(database);
        OrcamentoSqliteRepository orcamentoRepo = new OrcamentoSqliteRepository(database);
        OrdemDeServicoSqliteRepository osRepo = new OrdemDeServicoSqliteRepository(database);

        // Casos de uso
        CadastrarCliente cadastrarCliente = new CadastrarCliente(clienteRepo);
        CadastrarVeiculo cadastrarVeiculo = new CadastrarVeiculo(veiculoRepo, clienteRepo);
        CadastrarPeca cadastrarPeca = new CadastrarPeca(pecaRepo);
        AbrirOrdemDeServico abrirOS = new AbrirOrdemDeServico(osRepo, clienteRepo, veiculoRepo);
        RegistrarDiagnosticoNaOS registrarDiagnostico = new RegistrarDiagnosticoNaOS(osRepo);
        MontarOrcamentoDaOS montarOrcamento = new MontarOrcamentoDaOS(osRepo, orcamentoRepo, pecaRepo);
        AprovarOrcamentoDaOS aprovar = new AprovarOrcamentoDaOS(osRepo, orcamentoRepo);
        RejeitarOrcamentoDaOS rejeitar = new RejeitarOrcamentoDaOS(osRepo, orcamentoRepo);
        ConcluirServico concluir = new ConcluirServico(osRepo);
        EntregarVeiculo entregar = new EntregarVeiculo(osRepo);
        CancelarOrdemDeServico cancelar = new CancelarOrdemDeServico(osRepo);

        // Controllers
        ClienteController clienteCtrl = new ClienteController(cadastrarCliente, clienteRepo);
        VeiculoController veiculoCtrl = new VeiculoController(cadastrarVeiculo, veiculoRepo);
        PecaController pecaCtrl = new PecaController(cadastrarPeca, pecaRepo);
        OrdemDeServicoController osCtrl = new OrdemDeServicoController(
            abrirOS, registrarDiagnostico, montarOrcamento,
            aprovar, rejeitar, concluir, entregar, cancelar, osRepo
        );

        this.app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            config.staticFiles.add(sf -> {
                sf.directory = "/web";
                sf.location = Location.CLASSPATH;
            });
        });

        ManipuladorDeExcecoes.registrar(app);

        clienteCtrl.registrar(app);
        veiculoCtrl.registrar(app);
        pecaCtrl.registrar(app);
        osCtrl.registrar(app);
    }

    public ServidorOficina iniciar(int porta) {
        app.start(porta);
        return this;
    }

    public int porta() {
        return app.port();
    }

    public void parar() {
        app.stop();
        database.close();
    }

    public static void main(String[] args) {
        new ServidorOficina("jdbc:sqlite:oficina.db").iniciar(8080);
    }
}
