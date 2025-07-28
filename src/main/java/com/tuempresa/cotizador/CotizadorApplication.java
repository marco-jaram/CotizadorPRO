package com.tuempresa.cotizador;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CotizadorApplication {

	public static void main(String[] args) {


		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();


		String dbName = dotenv.get("DB_NAME");
		String dbPort = dotenv.get("DB_PORT");


		String dbUrl = "jdbc:mysql://localhost:" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=UTC";


		System.setProperty("DB_URL", dbUrl);
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));


		SpringApplication.run(CotizadorApplication.class, args);
	}

}
