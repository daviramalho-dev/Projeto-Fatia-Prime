package com.example.Fatia.Prime;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	boolean existsByCodigo(String codigo);

	@Query("select distinct p from Pedido p join fetch p.usuario u left join fetch p.itens i left join fetch i.produto order by p.dataCriacao desc")
	List<Pedido> findAllForAdmin();

	@Query("select distinct p from Pedido p join fetch p.usuario u left join fetch p.itens i left join fetch i.produto where p.id = :id")
	java.util.Optional<Pedido> findByIdForAdmin(Long id);
}
