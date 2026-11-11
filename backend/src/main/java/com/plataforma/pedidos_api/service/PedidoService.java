package com.plataforma.pedidos_api.service;

import com.plataforma.pedidos_api.dto.ItemPedidoRequest;
import com.plataforma.pedidos_api.dto.PedidoRequest;
import com.plataforma.pedidos_api.entity.ItemPedido;
import com.plataforma.pedidos_api.entity.Pedido;
import com.plataforma.pedidos_api.entity.Produto;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import com.plataforma.pedidos_api.repository.ItemPedidoRepository;
import com.plataforma.pedidos_api.repository.PedidoRepository;
import com.plataforma.pedidos_api.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ComercioRepository comercioRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ItemPedidoRepository itemPedidoRepository,
                         ProdutoRepository produtoRepository,
                         ComercioRepository comercioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.comercioRepository = comercioRepository;
    }

    @Transactional
    public Pedido criar(PedidoRequest request) {
        if (!comercioRepository.existsById(request.comercioId())) {
            throw new IllegalArgumentException("Comércio não encontrado");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<ItemPedido> itens = new ArrayList<>();

        for (ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + itemRequest.produtoId()));

            if (!produto.getComercioId().equals(request.comercioId())) {
                throw new IllegalArgumentException(
                        "O produto " + produto.getNome() + " não pertence a este comércio");
            }
            if (!produto.isDisponivel()) {
                throw new IllegalArgumentException(
                        "O produto " + produto.getNome() + " está indisponível");
            }

            ItemPedido item = new ItemPedido();
            item.setProdutoId(produto.getId());
            item.setQuantidade(itemRequest.quantidade());
            item.setPrecoUnitario(produto.getPreco());
            itens.add(item);

            total = total.add(produto.getPreco()
                    .multiply(BigDecimal.valueOf(itemRequest.quantidade())));
        }

        Pedido pedido = new Pedido();
        pedido.setComercioId(request.comercioId());
        pedido.setNomeCliente(request.nomeCliente());
        pedido.setTelefoneCliente(request.telefoneCliente());
        pedido.setTotal(total);
        Pedido salvo = pedidoRepository.save(pedido);

        for (ItemPedido item : itens) {
            item.setPedidoId(salvo.getId());
        }
        itemPedidoRepository.saveAll(itens);

        return salvo;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorComercio(UUID comercioId) {
    return pedidoRepository.findByComercioIdOrderByCriadoEmDesc(comercioId);
}
}