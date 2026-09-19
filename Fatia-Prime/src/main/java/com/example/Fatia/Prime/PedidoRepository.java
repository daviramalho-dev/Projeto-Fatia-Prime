package com.example.Fatia.Prime;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	boolean existsByCodigo(String codigo);

	@Query("select distinct p from Pedido p left join fetch p.usuario u left join fetch p.itens i left join fetch i.produto where p.codigo = :codigo")
	Optional<Pedido> findByCodigoForConsulta(@Param("codigo") String codigo);

	@Query("select distinct p from Pedido p left join fetch p.usuario u left join fetch p.itens i left join fetch i.produto "
		+ "where function('regexp_replace', coalesce(p.clienteTelefone, u.telefone, ''), '[^0-9]', '', 'g') = :telefone "
		+ "order by p.dataCriacao desc")
	List<Pedido> findAllByTelefoneForConsulta(@Param("telefone") String telefone);

	@Query("select distinct p from Pedido p left join fetch p.usuario u left join fetch p.itens i left join fetch i.produto order by p.dataCriacao desc")
	List<Pedido> findAllForAdmin();

	@Query("select distinct p from Pedido p left join fetch p.usuario u left join fetch p.itens i left join fetch i.produto where p.id = :id")
	java.util.Optional<Pedido> findByIdForAdmin(Long id);
}
