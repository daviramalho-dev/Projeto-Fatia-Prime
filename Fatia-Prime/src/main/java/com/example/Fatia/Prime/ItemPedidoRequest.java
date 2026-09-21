package com.example.Fatia.Prime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequest(
    @NotNull(message = "O produto é obrigatório")
    @Positive(message = "O produto deve ser válido")
    Long produtoId,
    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    @Max(value = 50, message = "A quantidade não pode ser maior que 50")
    Integer quantidade
) {
}
