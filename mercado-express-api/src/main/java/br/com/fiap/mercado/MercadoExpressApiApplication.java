package br.com.fiap.mercado;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class MercadoExpressApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MercadoExpressApiApplication.class, args);
		System.out.println("Rodando API - Mercado Express");
	}

}
