package com.plataforma.pedidos_api.controller;

import com.plataforma.pedidos_api.entity.Produto;
import com.plataforma.pedidos_api.repository.ProdutoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // Listar produtos de um comércio específico (Ex: /produtos/comercio/{id})
    @GetMapping("/comercio/{comercioId}")
    public List<Produto> listarPorComercio(@PathVariable UUID comercioId) {
        return produtoRepository.findByComercioId(comercioId);
    }

    // Cadastrar novo produto
    @PostMapping
    public Produto criarProduto(@RequestBody Produto produto) {
        return produtoRepository.save(produto);
    }
}