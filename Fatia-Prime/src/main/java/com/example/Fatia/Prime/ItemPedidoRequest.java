package com.example.Fatia.Prime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(
    @NotNull(message = "O produto é obrigatório")
    Long produtoId,
    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    Integer quantidade
) {
}
