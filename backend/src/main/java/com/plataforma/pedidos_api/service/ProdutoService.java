package com.plataforma.pedidos_api.service;

import com.plataforma.pedidos_api.dto.ProdutoRequest;
import com.plataforma.pedidos_api.dto.ProdutoResponse;
import com.plataforma.pedidos_api.entity.Produto;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import com.plataforma.pedidos_api.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ComercioRepository comercioRepository;

    public ProdutoService(ProdutoRepository produtoRepository,
                          ComercioRepository comercioRepository) {
        this.produtoRepository = produtoRepository;
        this.comercioRepository = comercioRepository;
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        if (!comercioRepository.existsById(request.comercioId())) {
            throw new IllegalArgumentException("Comércio não encontrado");
        }

        Produto produto = new Produto();
        produto.setComercioId(request.comercioId());
        produto.setNome(request.nome().trim());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());

        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarPorComercio(UUID comercioId) {
        return produtoRepository.findByComercioId(comercioId).stream()
                .map(ProdutoResponse::de)
                .toList();
    }
}