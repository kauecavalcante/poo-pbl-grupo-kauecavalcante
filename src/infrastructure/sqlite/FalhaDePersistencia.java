package infrastructure.sqlite;

public class FalhaDePersistencia extends RuntimeException {

    public FalhaDePersistencia(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
