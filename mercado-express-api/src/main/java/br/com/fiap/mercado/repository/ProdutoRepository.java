package br.com.fiap.mercado.repository;

import br.com.fiap.mercado.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findAllByOrderByCategoriaAscNomeAsc();
}
