package com.plataforma.pedidos_api.service;

import com.plataforma.pedidos_api.dto.AtualizarStatusRequest;
import com.plataforma.pedidos_api.dto.ItemPedidoRequest;
import com.plataforma.pedidos_api.dto.ItemPedidoResponse;
import com.plataforma.pedidos_api.dto.PedidoRequest;
import com.plataforma.pedidos_api.dto.PedidoResponse;
import com.plataforma.pedidos_api.entity.ItemPedido;
import com.plataforma.pedidos_api.entity.Pedido;
import com.plataforma.pedidos_api.entity.Produto;
import com.plataforma.pedidos_api.entity.StatusPedido;
import com.plataforma.pedidos_api.exception.RecursoNaoEncontradoException;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import com.plataforma.pedidos_api.repository.ItemPedidoRepository;
import com.plataforma.pedidos_api.repository.PedidoRepository;
import com.plataforma.pedidos_api.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<PedidoResponse> listarPorComercio(UUID comercioId) {
        List<Pedido> pedidos = pedidoRepository.findByComercioIdOrderByCriadoEmDesc(comercioId);
        if (pedidos.isEmpty()) {
            return List.of();
        }

        List<UUID> pedidoIds = pedidos.stream().map(Pedido::getId).toList();
        List<ItemPedido> itens = itemPedidoRepository.findByPedidoIdIn(pedidoIds);

        Map<UUID, String> nomesProdutos = produtoRepository
                .findAllById(itens.stream().map(ItemPedido::getProdutoId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Produto::getId, Produto::getNome));

        Map<UUID, List<ItemPedido>> itensPorPedido = itens.stream()
                .collect(Collectors.groupingBy(ItemPedido::getPedidoId));

        return pedidos.stream()
                .map(p -> new PedidoResponse(
                        p.getId(),
                        p.getComercioId(),
                        p.getNomeCliente(),
                        p.getTelefoneCliente(),
                        p.getStatus(),
                        p.getTotal(),
                        p.getCriadoEm(),
                        itensPorPedido.getOrDefault(p.getId(), List.of()).stream()
                                .map(i -> new ItemPedidoResponse(
                                        i.getProdutoId(),
                                        nomesProdutos.get(i.getProdutoId()),
                                        i.getQuantidade(),
                                        i.getPrecoUnitario(),
                                        i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))))
                                .toList()))
                .toList();
    }

    @Transactional
    public Pedido atualizarStatus(UUID pedidoId, AtualizarStatusRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));

        StatusPedido atual = StatusPedido.deValor(pedido.getStatus());
        if (atual.finalizado()) {
            throw new IllegalArgumentException(
                    "Pedido já está " + atual.getValor() + " e não pode mais ser alterado");
        }

        StatusPedido novo = StatusPedido.deValor(request.status());
        pedido.setStatus(novo.getValor());
        return pedidoRepository.save(pedido);
    }
}