package br.com.fiap.mercado.service;

import br.com.fiap.mercado.model.ItemPedido;
import br.com.fiap.mercado.model.Pedido;
import br.com.fiap.mercado.model.Produto;
import br.com.fiap.mercado.model.StatusPedido;
import br.com.fiap.mercado.repository.EmailEnviadoRepository;
import br.com.fiap.mercado.repository.PedidoRepository;
import br.com.fiap.mercado.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Calcula os indicadores do Dashboard da gerencia (vendas, lucros, estoque, status). */
@Service
public class DashboardService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private EmailEnviadoRepository emailRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> indicadores() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByDataCriacaoDesc();
        List<Pedido> pagos = pedidos.stream().filter(p -> p.getStatus() == StatusPedido.PAGO).toList();

        BigDecimal faturamento = soma(pagos.stream().map(Pedido::getValorTotal).toList());
        BigDecimal custo = soma(pagos.stream().map(Pedido::getCustoTotal).toList());
        BigDecimal lucro = faturamento.subtract(custo);
        BigDecimal ticketMedio = pagos.isEmpty() ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(pagos.size()), 2, RoundingMode.HALF_UP);

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("faturamento", faturamento);
        d.put("custo", custo);
        d.put("lucro", lucro);
        d.put("ticketMedio", ticketMedio);
        d.put("totalPedidos", pedidos.size());
        d.put("pedidosPagos", pagos.size());
        d.put("pedidosRecusados", pedidos.stream().filter(p -> p.getStatus() == StatusPedido.RECUSADO).count());
        d.put("pedidosAguardando", pedidos.stream().filter(p -> p.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO).count());
        d.put("emailsEnviados", emailRepository.count());

        // Vendas e lucro por dia (ultimos 7 dias)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, BigDecimal[]> porDia = new TreeMap<>();
        for (int i = 6; i >= 0; i--) porDia.put(LocalDate.now().minusDays(i), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        for (Pedido p : pagos) {
            BigDecimal[] v = porDia.get(p.getDataCriacao().toLocalDate());
            if (v != null) { v[0] = v[0].add(p.getValorTotal()); v[1] = v[1].add(p.getLucro()); }
        }
        d.put("diasLabels", porDia.keySet().stream().map(fmt::format).toList());
        d.put("diasVendas", porDia.values().stream().map(v -> v[0]).toList());
        d.put("diasLucro", porDia.values().stream().map(v -> v[1]).toList());

        // Por produto: quantidade vendida, receita e lucro
        Map<String, long[]> qtd = new LinkedHashMap<>();
        Map<String, BigDecimal[]> valores = new LinkedHashMap<>();
        for (Pedido p : pagos) {
            for (ItemPedido i : p.getItens()) {
                String nome = i.getProduto().getNome();
                qtd.computeIfAbsent(nome, k -> new long[1])[0] += i.getQuantidade();
                BigDecimal[] v = valores.computeIfAbsent(nome, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                BigDecimal q = BigDecimal.valueOf(i.getQuantidade());
                v[0] = v[0].add(i.getPrecoUnitario().multiply(q));
                v[1] = v[1].add(i.getPrecoUnitario().subtract(i.getCustoUnitario()).multiply(q));
            }
        }
        d.put("produtosLabels", new ArrayList<>(valores.keySet()));
        d.put("produtosQtd", qtd.values().stream().map(v -> v[0]).toList());
        d.put("produtosReceita", valores.values().stream().map(v -> v[0]).toList());
        d.put("produtosLucro", valores.values().stream().map(v -> v[1]).toList());

        // Estoque atual
        List<Produto> produtos = produtoRepository.findAllByOrderByCategoriaAscNomeAsc();
        d.put("estoqueLabels", produtos.stream().map(Produto::getNome).toList());
        d.put("estoqueValores", produtos.stream().map(Produto::getEstoque).toList());
        d.put("estoqueBaixo", produtos.stream().filter(p -> p.getEstoque() < 10).map(Produto::getNome).toList());

        d.put("statusLabels", List.of("Pagos", "Recusados", "Aguardando"));
        d.put("statusValores", List.of(d.get("pedidosPagos"), d.get("pedidosRecusados"), d.get("pedidosAguardando")));
        return d;
    }

    private BigDecimal soma(List<BigDecimal> valores) {
        return valores.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
