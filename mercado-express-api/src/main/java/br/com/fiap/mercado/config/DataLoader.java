package br.com.fiap.mercado.config;

import br.com.fiap.mercado.model.Produto;
import br.com.fiap.mercado.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Produtos iniciais do Mercado Express (criados so na primeira execucao). */
@Component
public class DataLoader implements CommandLineRunner {

    private final ProdutoRepository repository;

    public DataLoader(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        salvar("Banana Prata (kg)", "Hortifruti", "🍌", "3.20", "6.99", 80);
        salvar("Maca Gala (kg)", "Hortifruti", "🍎", "5.10", "9.90", 60);
        salvar("Morango (bandeja 250g)", "Hortifruti", "🍓", "4.50", "8.49", 35);
        salvar("Abacate (un)", "Hortifruti", "🥑", "2.80", "5.99", 40);
        salvar("Detergente Neutro 500ml", "Limpeza", "🧴", "1.40", "2.79", 120);
        salvar("Sabao em Po 1kg", "Limpeza", "🧼", "8.90", "15.90", 45);
        salvar("Agua Sanitaria 2L", "Limpeza", "🫧", "3.10", "6.49", 50);
        salvar("Kit 3 Meias Esportivas", "Vestuario", "🧦", "12.00", "29.90", 25);
        salvar("Pao Frances (kg)", "Padaria", "🥖", "7.50", "14.90", 30);
        salvar("Leite Integral 1L", "Mercearia", "🥛", "3.60", "5.49", 90);
        System.out.println("Produtos de exemplo do Mercado Express criados.");
    }

    private void salvar(String nome, String categoria, String emoji, String custo, String venda, int estoque) {
        repository.save(Produto.builder().nome(nome).categoria(categoria).emoji(emoji)
                .precoCusto(new BigDecimal(custo)).precoVenda(new BigDecimal(venda)).estoque(estoque).build());
    }
}
