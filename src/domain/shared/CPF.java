package domain.shared;

public final class CPF {

    private static final int TAMANHO = 11;

    private final String numero;

    private CPF(String numero) {
        this.numero = numero;
    }

    public static CPF de(String entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("cpf não pode ser nulo");
        }
        String limpo = entrada.replace(".", "").replace("-", "").replace(" ", "");
        if (limpo.length() != TAMANHO || !contemSomenteDigitos(limpo)) {
            throw new IllegalArgumentException("cpf deve ter 11 dígitos");
        }
        if (todosDigitosIguais(limpo)) {
            throw new IllegalArgumentException("cpf inválido");
        }
        return new CPF(limpo);
    }

    public String numero() {
        return numero;
    }

    private static boolean contemSomenteDigitos(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean todosDigitosIguais(String s) {
        char primeiro = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }
}
