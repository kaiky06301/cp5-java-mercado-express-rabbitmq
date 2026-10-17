package br.com.fiap.banco.repository;

import br.com.fiap.banco.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    Optional<ContaBancaria> findByEmailIgnoreCase(String email);
}
