package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long usuarioId,
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
            pedido.getCodigo(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            pedido.getValorTotal(),
            pedido.getObservacoes(),
            itens
        );
    }
}
