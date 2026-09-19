package com.example.Fatia.Prime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdminProdutoRequest(
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 150, message = "O nome do produto deve ter no máximo 150 caracteres")
    String nome,

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    String descricao,

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O preço não pode ser negativo")
    BigDecimal preco,

    @Size(max = 500, message = "A imagem deve ter no máximo 500 caracteres")
    String imagem,

    @NotNull(message = "A categoria é obrigatória")
    Long categoriaId,

    Boolean ativo
) {
}