package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record FreteResponse(
    String cep,
    String regiao,
    BigDecimal valorFrete,
    double distanciaKm,
    String fonteCoordenadas
) {
}
