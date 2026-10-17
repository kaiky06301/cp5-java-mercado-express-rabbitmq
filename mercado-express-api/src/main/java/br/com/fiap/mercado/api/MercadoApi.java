package br.com.fiap.mercado.api;

import br.com.fiap.mercado.model.EmailEnviado;
import br.com.fiap.mercado.model.Produto;
import br.com.fiap.mercado.repository.EmailEnviadoRepository;
import br.com.fiap.mercado.repository.ProdutoRepository;
import br.com.fiap.mercado.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MercadoApi {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private EmailEnviadoRepository emailRepository;
    @Autowired private DashboardService dashboardService;

    @GetMapping("/produtos")
    public List<Produto> produtos() {
        return produtoRepository.findAllByOrderByCategoriaAscNomeAsc();
    }

    @GetMapping("/emails")
    public List<EmailEnviado> emails() {
        return emailRepository.findAllByOrderByDataEnvioDesc();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.indicadores();
    }
}
