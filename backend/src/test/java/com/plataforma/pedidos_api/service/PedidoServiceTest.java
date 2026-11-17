package com.plataforma.pedidos_api.service;

import com.plataforma.pedidos_api.dto.ItemPedidoRequest;
import com.plataforma.pedidos_api.dto.PedidoRequest;
import com.plataforma.pedidos_api.entity.Pedido;
import com.plataforma.pedidos_api.entity.Produto;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import com.plataforma.pedidos_api.repository.ItemPedidoRepository;
import com.plataforma.pedidos_api.repository.PedidoRepository;
import com.plataforma.pedidos_api.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ComercioRepository comercioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void criarPedido_calculaOTotalPeloPrecoDoBanco() {
        // Arrange: monta o cenário
        UUID comercioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        Produto produto = new Produto();
        produto.setComercioId(comercioId);
        produto.setNome("X-Burger");
        produto.setPreco(new BigDecimal("18.50"));

        when(comercioRepository.existsById(comercioId)).thenReturn(true);
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PedidoRequest request = new PedidoRequest(
                comercioId, "Maria", "81999999999",
                List.of(new ItemPedidoRequest(produtoId, 2)));

        // Act: executa o que está sendo testado
        Pedido pedido = pedidoService.criar(request);

        // Assert: confere o resultado (2 x 18,50 = 37,00)
        assertThat(pedido.getTotal()).isEqualByComparingTo("37.00");    }
}