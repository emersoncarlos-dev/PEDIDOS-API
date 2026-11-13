package com.plataforma.pedidos_api.dto;

import com.plataforma.pedidos_api.entity.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        UUID comercioId,
        String nome,
        String descricao,
        BigDecimal preco,
        boolean disponivel
) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getComercioId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.isDisponivel());
    }
}