package com.example.Fatia.Prime;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrueOrderByNomeAsc();

    boolean existsByCategoria_Id(Long categoriaId);

    long countByAtivoTrue();
}
