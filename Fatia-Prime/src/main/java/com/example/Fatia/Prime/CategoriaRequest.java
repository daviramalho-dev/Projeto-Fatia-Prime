package com.example.Fatia.Prime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Validated payload for creating or renaming a category.
 *
 * @param nome required category name, limited to 100 characters
 */
public record CategoriaRequest(
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 100, message = "O nome da categoria não pode exceder 100 caracteres")
    String nome
) {
}
