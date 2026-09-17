package com.example.Fatia.Prime;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record CadastroRequest(
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    String nome,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 64, message = "A senha deve ter entre 6 e 64 caracteres")
    String senha
) {
    public CadastroRequest {
        nome = nome == null ? null : nome.trim();
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}