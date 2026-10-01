package com.example.Fatia.Prime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdminOpcaoPizzaRequest(
    @NotBlank(message = "O nome da opção é obrigatório")
    @Size(max = 100, message = "O nome da opção deve ter no máximo 100 caracteres")
    String nome,
    @NotNull(message = "O tipo da opção é obrigatório")
    TipoOpcaoPizza tipo,
    @NotNull(message = "O tipo de pizza é obrigatório")
    TipoProdutoPizza tipoProduto,
    @NotNull(message = "O preço adicional é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço adicional não pode ser negativo")
    BigDecimal precoAdicional,
    Boolean ativo
) {
}
