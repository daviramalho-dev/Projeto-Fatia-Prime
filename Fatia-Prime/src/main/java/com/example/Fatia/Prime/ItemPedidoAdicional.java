package com.example.Fatia.Prime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class ItemPedidoAdicional {

    @Column(name = "opcao_id")
    private Long opcaoId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "preco_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAdicional;

    protected ItemPedidoAdicional() {
    }

    public ItemPedidoAdicional(Long opcaoId, String nome, BigDecimal precoAdicional) {
        this.opcaoId = opcaoId;
        this.nome = nome;
        this.precoAdicional = precoAdicional;
    }

    public Long getOpcaoId() {
        return opcaoId;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPrecoAdicional() {
        return precoAdicional;
    }
}
