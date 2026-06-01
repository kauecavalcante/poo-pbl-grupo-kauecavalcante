package presentation.dto;

import java.util.List;

public record MontarOrcamentoRequest(
    List<ItemDePecaInputDto> pecas,
    List<ItemDeMaoDeObraInputDto> maosDeObra
) {}
