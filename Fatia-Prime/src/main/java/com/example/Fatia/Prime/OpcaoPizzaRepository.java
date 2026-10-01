package com.example.Fatia.Prime;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcaoPizzaRepository extends JpaRepository<OpcaoPizza, Long> {
    List<OpcaoPizza> findByAtivoTrueAndTipoProdutoOrderByTipoAscNomeAsc(TipoProdutoPizza tipoProduto);

    List<OpcaoPizza> findAllByOrderByTipoAscTipoProdutoAscNomeAsc();
}
