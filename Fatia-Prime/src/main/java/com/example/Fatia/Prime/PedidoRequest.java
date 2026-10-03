package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PedidoRequest(
    Long usuarioId,
    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(max = 100, message = "O nome do cliente deve ter no máximo 100 caracteres")
    String clienteNome,
    @NotBlank(message = "O telefone do cliente é obrigatório")
    @Size(max = 20, message = "O telefone do cliente deve ter no máximo 20 caracteres")
    String clienteTelefone,
    @Size(max = 500, message = "O endereço deve ter no máximo 500 caracteres")
    @NotBlank(message = "O endereço de entrega é obrigatório")
    String endereco,
    @NotBlank(message = "Informe seu CEP.")
    @Size(max = 9, message = "Informe um CEP válido.")
    String cep,
    @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
    String observacoes,
    @NotEmpty(message = "O pedido deve conter pelo menos um item")
    List<@NotNull @Valid ItemPedidoRequest> itens
) {
}
