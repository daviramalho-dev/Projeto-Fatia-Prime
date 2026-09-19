package com.example.Fatia.Prime;

import jakarta.validation.constraints.NotBlank;

public record AdminProdutoStatusRequest(
    @NotBlank(message = "O status é obrigatório")
    String status
) {
}