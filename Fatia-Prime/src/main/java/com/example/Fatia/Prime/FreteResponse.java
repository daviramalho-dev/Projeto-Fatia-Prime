package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record FreteResponse(
    String cep,
    String regiao,
    BigDecimal valorFrete
) {
    public static FreteResponse de(String cep, FaixaFrete faixa) {
        return new FreteResponse(cep, faixa.getNome(), faixa.getValorFrete());
    }
}
