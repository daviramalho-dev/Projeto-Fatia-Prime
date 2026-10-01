package com.example.Fatia.Prime;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaixaFreteRepository extends JpaRepository<FaixaFrete, Long> {
    Optional<FaixaFrete> findFirstByAtivoTrueAndCepInicialLessThanEqualAndCepFinalGreaterThanEqualOrderByCepInicialAsc(
        String cepInicial,
        String cepFinal
    );
}
