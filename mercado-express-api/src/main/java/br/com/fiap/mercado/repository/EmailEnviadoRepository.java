package br.com.fiap.mercado.repository;

import br.com.fiap.mercado.model.EmailEnviado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {
    List<EmailEnviado> findAllByOrderByDataEnvioDesc();
}
