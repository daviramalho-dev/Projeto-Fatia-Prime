package com.example.Fatia.Prime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ItemPedidoRequest(
    @NotNull(message = "O produto é obrigatório")
    @Positive(message = "O produto deve ser válido")
    Long produtoId,
    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    @Max(value = 50, message = "A quantidade não pode ser maior que 50")
    Integer quantidade,
    TipoPizza tipoPizza,
    @Positive(message = "O segundo sabor deve ser válido")
    Long segundoProdutoId,
    @Positive(message = "A borda deve ser válida")
    Long bordaId,
    @Size(max = 20, message = "O pedido não pode conter mais de 20 adicionais")
    List<@NotNull @Positive(message = "O adicional deve ser válido") Long> adicionalIds,
    @Size(max = 20, message = "O pedido não pode conter mais de 20 molhos")
    List<@NotNull @Positive(message = "O molho deve ser válido") Long> molhoIds
) {
    public ItemPedidoRequest {
        adicionalIds = adicionalIds == null ? List.of() : adicionalIds;
        molhoIds = molhoIds == null ? List.of() : molhoIds;
    }
}
