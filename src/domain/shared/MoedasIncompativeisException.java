package domain.shared;

import java.util.Currency;

public class MoedasIncompativeisException extends RuntimeException {

    public MoedasIncompativeisException(Currency esquerda, Currency direita) {
        super("operação entre moedas distintas: "
            + esquerda.getCurrencyCode() + " e " + direita.getCurrencyCode());
    }
}
