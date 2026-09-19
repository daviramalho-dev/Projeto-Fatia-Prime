package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ConsultaPedidoResponse(
    Long id,
    Long usuarioId,
    String codigo,
    String status,
    LocalDateTime dataCriacao,
    BigDecimal valorTotal,
    String observacoes,
    String nomeCliente,
    String emailCliente,
    String telefone,
    List<ConsultaItemPedidoResponse> itens
) {
    public static ConsultaPedidoResponse de(Pedido pedido) {
        Usuario usuario = pedido.getUsuario();
        return new ConsultaPedidoResponse(
            pedido.getId(),
            usuario != null ? usuario.getId() : null,
            pedido.getCodigo(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            pedido.getValorTotal(),
            pedido.getObservacoes(),
            usuario != null ? usuario.getNome() : null,
            usuario != null ? usuario.getEmail() : null,
            usuario != null ? usuario.getTelefone() : null,
            pedido.getItens().stream().map(ConsultaItemPedidoResponse::de).toList()
        );
    }
}