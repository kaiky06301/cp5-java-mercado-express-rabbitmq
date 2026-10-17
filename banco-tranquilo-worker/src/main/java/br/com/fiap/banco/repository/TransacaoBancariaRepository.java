package br.com.fiap.banco.repository;

import br.com.fiap.banco.model.TransacaoBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoBancariaRepository extends JpaRepository<TransacaoBancaria, Long> {
}
