package com.limpacao;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LimpacaoApplication {

	/*
	 * Fuso usado para gravar as datas: UTC, o mesmo do contêiner do Render,
	 * para que o horário não mude conforme a máquina onde o back roda
	 * (Render, Docker, Windows, Mac...). A API devolve as datas com o fuso
	 * e o navegador converte para o fuso de quem está usando o sistema.
	 * Pode ser trocado pela variável de ambiente APP_TIMEZONE.
	 */
	static final String FUSO_PADRAO = "UTC";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(
				System.getenv().getOrDefault("APP_TIMEZONE", FUSO_PADRAO)));
		SpringApplication.run(LimpacaoApplication.class, args);
	}

}
