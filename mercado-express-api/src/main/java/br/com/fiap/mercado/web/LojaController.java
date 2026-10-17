package br.com.fiap.mercado.web;

import br.com.fiap.mercado.dto.CompraRequestDTO;
import br.com.fiap.mercado.dto.CompraResponseDTO;
import br.com.fiap.mercado.facade.PagamentoFacade;
import br.com.fiap.mercado.model.Produto;
import br.com.fiap.mercado.repository.PedidoRepository;
import br.com.fiap.mercado.repository.ProdutoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

/** Telas da loja: catalogo, carrinho e acompanhamento do pedido. */
@Controller
public class LojaController {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PagamentoFacade pagamentoFacade;

    @GetMapping("/")
    public String loja(Model model, HttpSession session) {
        model.addAttribute("produtos", produtoRepository.findAllByOrderByCategoriaAscNomeAsc());
        model.addAttribute("qtdCarrinho", carrinho(session).values().stream().mapToInt(Integer::intValue).sum());
        return "loja";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionar(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") Integer quantidade,
                            HttpSession session, RedirectAttributes ra) {
        carrinho(session).merge(produtoId, quantidade, Integer::sum);
        produtoRepository.findById(produtoId).ifPresent(p -> ra.addFlashAttribute("msg", p.getNome() + " adicionado ao carrinho"));
        return "redirect:/";
    }

    @PostMapping("/carrinho/remover")
    public String remover(@RequestParam Long produtoId, HttpSession session) {
        carrinho(session).remove(produtoId);
        return "redirect:/carrinho";
    }

    @GetMapping("/carrinho")
    public String verCarrinho(Model model, HttpSession session) {
        List<Map<String, Object>> linhas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : carrinho(session).entrySet()) {
            Optional<Produto> p = produtoRepository.findById(e.getKey());
            if (p.isEmpty()) continue;
            BigDecimal subtotal = p.get().getPrecoVenda().multiply(BigDecimal.valueOf(e.getValue()));
            total = total.add(subtotal);
            linhas.add(Map.of("produto", p.get(), "quantidade", e.getValue(), "subtotal", subtotal));
        }
        model.addAttribute("linhas", linhas);
        model.addAttribute("total", total);
        return "carrinho";
    }

    @PostMapping("/carrinho/finalizar")
    public String finalizar(@RequestParam String nomeCliente, @RequestParam String emailCliente,
                            HttpSession session, RedirectAttributes ra) {
        Map<Long, Integer> carrinho = carrinho(session);
        if (carrinho.isEmpty()) {
            ra.addFlashAttribute("erro", "Carrinho vazio");
            return "redirect:/carrinho";
        }
        CompraRequestDTO request = new CompraRequestDTO();
        request.setNomeCliente(nomeCliente);
        request.setEmailCliente(emailCliente);
        carrinho.forEach((id, qtd) -> {
            CompraRequestDTO.Item item = new CompraRequestDTO.Item();
            item.setProdutoId(id);
            item.setQuantidade(qtd);
            request.getItens().add(item);
        });
        try {
            CompraResponseDTO resposta = pagamentoFacade.solicitarPagamento(request);
            carrinho.clear();
            return "redirect:/pedidos/" + resposta.getNumeroPedido();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/carrinho";
        }
    }

    @GetMapping("/pedidos/{numeroPedido}")
    public String pedido(@PathVariable String numeroPedido, Model model) {
        pedidoRepository.findByNumeroPedido(numeroPedido).ifPresent(p -> model.addAttribute("pedido", p));
        return "pedido";
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> carrinho(HttpSession session) {
        Map<Long, Integer> c = (Map<Long, Integer>) session.getAttribute("carrinho");
        if (c == null) {
            c = new LinkedHashMap<>();
            session.setAttribute("carrinho", c);
        }
        return c;
    }
}
