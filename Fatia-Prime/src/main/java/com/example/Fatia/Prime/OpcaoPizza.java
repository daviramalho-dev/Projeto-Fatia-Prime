package com.example.Fatia.Prime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "opcoes_pizza")
public class OpcaoPizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOpcaoPizza tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false, length = 20)
    private TipoProdutoPizza tipoProduto;

    @Column(name = "preco_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAdicional;

    @Column(nullable = false)
    private boolean ativo = true;

    public OpcaoPizza() {
    }

    public OpcaoPizza(String nome, TipoOpcaoPizza tipo, TipoProdutoPizza tipoProduto, BigDecimal precoAdicional) {
        this.nome = nome;
        this.tipo = tipo;
        this.tipoProduto = tipoProduto;
        this.precoAdicional = precoAdicional;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoOpcaoPizza getTipo() {
        return tipo;
    }

    public void setTipo(TipoOpcaoPizza tipo) {
        this.tipo = tipo;
    }

    public TipoProdutoPizza getTipoProduto() {
        return tipoProduto;
    }

    public void setTipoProduto(TipoProdutoPizza tipoProduto) {
        this.tipoProduto = tipoProduto;
    }

    public BigDecimal getPrecoAdicional() {
        return precoAdicional;
    }

    public void setPrecoAdicional(BigDecimal precoAdicional) {
        this.precoAdicional = precoAdicional;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
