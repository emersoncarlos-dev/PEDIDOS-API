package com.plataforma.pedidos_api.service;

import com.plataforma.pedidos_api.dto.AtualizarStatusRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private Produto criarProduto(UUID comercioId, boolean disponivel) {
        Produto produto = new Produto();
        produto.setComercioId(comercioId);
        produto.setNome("X-Burger");
        produto.setPreco(new BigDecimal("18.50"));
        produto.setDisponivel(disponivel);
        return produto;
    }

    private PedidoRequest pedidoComUmItem(UUID comercioId, UUID produtoId) {
        return new PedidoRequest(
                comercioId, "Maria", "81999999999",
                List.of(new ItemPedidoRequest(produtoId, 2)));
    }

    @Test
    void criarPedido_calculaOTotalPeloPrecoDoBanco() {
        UUID comercioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        when(comercioRepository.existsById(comercioId)).thenReturn(true);
        when(produtoRepository.findById(produtoId))
                .thenReturn(Optional.of(criarProduto(comercioId, true)));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Pedido pedido = pedidoService.criar(pedidoComUmItem(comercioId, produtoId));

        assertThat(pedido.getTotal()).isEqualByComparingTo("37.00");
    }

    @Test
    void criarPedido_recusaProdutoIndisponivel() {
        UUID comercioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        when(comercioRepository.existsById(comercioId)).thenReturn(true);
        when(produtoRepository.findById(produtoId))
                .thenReturn(Optional.of(criarProduto(comercioId, false)));

        assertThatThrownBy(() -> pedidoService.criar(pedidoComUmItem(comercioId, produtoId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void criarPedido_recusaProdutoDeOutroComercio() {
        UUID comercioId = UUID.randomUUID();
        UUID outroComercioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        when(comercioRepository.existsById(comercioId)).thenReturn(true);
        when(produtoRepository.findById(produtoId))
                .thenReturn(Optional.of(criarProduto(outroComercioId, true)));

        assertThatThrownBy(() -> pedidoService.criar(pedidoComUmItem(comercioId, produtoId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence a este comércio");
    }

    @Test
    void criarPedido_recusaComercioInexistente_eNaoGravaNada() {
        UUID comercioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        when(comercioRepository.existsById(comercioId)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.criar(pedidoComUmItem(comercioId, produtoId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comércio não encontrado");

        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(produtoRepository, itemPedidoRepository);
    }

    @Test
    void atualizarStatus_recusaPedidoJaFinalizado() {
        UUID pedidoId = UUID.randomUUID();

        Pedido pedido = new Pedido();
        pedido.setStatus("cancelado");
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() ->
                pedidoService.atualizarStatus(pedidoId, new AtualizarStatusRequest("pronto")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode mais ser alterado");

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}