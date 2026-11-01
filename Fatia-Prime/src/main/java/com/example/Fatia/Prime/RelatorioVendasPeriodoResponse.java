package com.example.Fatia.Prime;

import java.time.LocalDate;

public record RelatorioVendasPeriodoResponse(
    LocalDate primeiroPedido,
    LocalDate hoje
) {
}
