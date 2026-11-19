package com.plataforma.pedidos_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração: sobe a aplicação inteira e conecta no banco.
 * Só roda quando a variável de ambiente DB_URL está definida.
 * Sem banco configurado (por exemplo, no CI), o teste é ignorado em vez de falhar.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class PedidosApiApplicationTests {

	@Test
	void contextLoads() {
	}

}