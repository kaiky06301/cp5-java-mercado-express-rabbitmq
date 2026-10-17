package br.com.fiap.mercado.repository;

import br.com.fiap.mercado.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    List<Pedido> findAllByOrderByDataCriacaoDesc();
}
