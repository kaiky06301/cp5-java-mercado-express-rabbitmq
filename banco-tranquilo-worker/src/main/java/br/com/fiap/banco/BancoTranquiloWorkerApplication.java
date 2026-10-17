package br.com.fiap.banco;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class BancoTranquiloWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoTranquiloWorkerApplication.class, args);
		System.out.println("Rodando Worker - Banco Tranquilo");
	}

}
