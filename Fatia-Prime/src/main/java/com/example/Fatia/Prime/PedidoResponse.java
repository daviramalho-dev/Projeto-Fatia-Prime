package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long usuarioId,
    String clienteNome,
    String clienteEmail,
    String clienteTelefone,
    String endereco,
    String codigo,
    String status,
    LocalDateTime dataCriacao,
    BigDecimal valorTotal,
    String observacoes,
    List<ItemPedidoResponse> itens
) {
    public static PedidoResponse de(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens() == null
            ? List.of()
            : pedido.getItens().stream().map(ItemPedidoResponse::de).toList();

        return new PedidoResponse(
            pedido.getId(),
            pedido.getUsuario() != null ? pedido.getUsuario().getId() : null,
            pedido.getClienteNome() != null ? pedido.getClienteNome() : pedido.getUsuario() != null ? pedido.getUsuario().getNome() : null,
            pedido.getClienteEmail() != null ? pedido.getClienteEmail() : pedido.getUsuario() != null ? pedido.getUsuario().getEmail() : null,
            pedido.getClienteTelefone() != null ? pedido.getClienteTelefone() : pedido.getUsuario() != null ? pedido.getUsuario().getTelefone() : null,
            pedido.getEndereco(),
            pedido.getCodigo(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            pedido.getValorTotal(),
            pedido.getObservacoes(),
            itens
        );
    }
}
