package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PedidoRequest(
    Long usuarioId,

    @Size(max = 100, message = "O nome do cliente deve ter no máximo 100 caracteres")
    String clienteNome,

    @Email(message = "E-mail do cliente inválido")
    @Size(max = 254, message = "O e-mail do cliente deve ter no máximo 254 caracteres")
    String clienteEmail,

    @Size(max = 20, message = "O telefone do cliente deve ter no máximo 20 caracteres")
    String clienteTelefone,

    @Size(max = 500, message = "O endereço deve ter no máximo 500 caracteres")
    String endereco,

    String observacoes,
    @NotNull(message = "O pedido deve conter itens")
    @Valid
    List<ItemPedidoRequest> itens
) {
    public PedidoRequest(Long usuarioId, String observacoes, List<ItemPedidoRequest> itens) {
        this(usuarioId, null, null, null, null, observacoes, itens);
    }
}
