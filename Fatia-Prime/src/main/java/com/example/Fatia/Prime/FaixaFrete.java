package com.example.Fatia.Prime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "faixas_frete")
public class FaixaFrete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "cep_inicial", nullable = false, length = 8)
    private String cepInicial;

    @Column(name = "cep_final", nullable = false, length = 8)
    private String cepFinal;

    @Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete;

    @Column(nullable = false)
    private boolean ativo = true;

    protected FaixaFrete() {
    }

    public FaixaFrete(String nome, String cepInicial, String cepFinal, BigDecimal valorFrete, boolean ativo) {
        this.nome = nome;
        this.cepInicial = cepInicial;
        this.cepFinal = cepFinal;
        this.valorFrete = valorFrete;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCepInicial() {
        return cepInicial;
    }

    public String getCepFinal() {
        return cepFinal;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
