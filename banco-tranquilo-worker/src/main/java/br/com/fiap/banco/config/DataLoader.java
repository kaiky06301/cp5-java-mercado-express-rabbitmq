package br.com.fiap.banco.config;

import br.com.fiap.banco.model.ContaBancaria;
import br.com.fiap.banco.repository.ContaBancariaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Contas de exemplo do Banco Tranquilo (criadas so na primeira execucao). */
@Component
public class DataLoader implements CommandLineRunner {

    private final ContaBancariaRepository repository;

    public DataLoader(ContaBancariaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        repository.save(ContaBancaria.builder().titular("Ana Souza").email("ana@tranquilo.com").saldo(new BigDecimal("5000.00")).build());
        repository.save(ContaBancaria.builder().titular("Bruno Lima").email("bruno@tranquilo.com").saldo(new BigDecimal("1500.00")).build());
        repository.save(ContaBancaria.builder().titular("Carla Dias").email("carla@tranquilo.com").saldo(new BigDecimal("20.00")).build());
        System.out.println("Contas de exemplo do Banco Tranquilo criadas.");
    }
}
