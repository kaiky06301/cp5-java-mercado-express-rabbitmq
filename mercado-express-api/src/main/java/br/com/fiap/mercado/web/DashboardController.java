package br.com.fiap.mercado.web;

import br.com.fiap.mercado.repository.EmailEnviadoRepository;
import br.com.fiap.mercado.repository.PedidoRepository;
import br.com.fiap.mercado.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Dashboard da gerencia do Mercado Express. */
@Controller
public class DashboardController {

    @Autowired private DashboardService dashboardService;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private EmailEnviadoRepository emailRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("d", dashboardService.indicadores());
        model.addAttribute("pedidos", pedidoRepository.findAllByOrderByDataCriacaoDesc());
        model.addAttribute("emails", emailRepository.findAllByOrderByDataEnvioDesc());
        return "dashboard";
    }
}
