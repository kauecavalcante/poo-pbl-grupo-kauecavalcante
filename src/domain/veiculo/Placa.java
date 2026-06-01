package domain.veiculo;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Placa {

    private static final Pattern ANTIGA = Pattern.compile("^[A-Z]{3}\\d{4}$");
    private static final Pattern MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");
    private static final int TAMANHO = 7;

    private final String valor;

    private Placa(String valor) {
        this.valor = valor;
    }

    public static Placa de(String entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("placa não pode ser nula");
        }
        String normalizada = entrada.replace("-", "")
            .replace(" ", "")
            .toUpperCase(Locale.ROOT);
        if (normalizada.length() != TAMANHO) {
            throw new IllegalArgumentException("placa deve ter 7 caracteres");
        }
        if (!ANTIGA.matcher(normalizada).matches() && !MERCOSUL.matcher(normalizada).matches()) {
            throw new IllegalArgumentException("placa inválida");
        }
        return new Placa(normalizada);
    }

    public String valor() {
        return valor;
    }

    public boolean ehAntiga() {
        return ANTIGA.matcher(valor).matches();
    }

    public boolean ehMercosul() {
        return MERCOSUL.matcher(valor).matches();
    }
}
