package com.plataforma.pedidos_api.repository;

import com.plataforma.pedidos_api.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    
    // Busca todos os produtos de um comércio específico
    List<Produto> findByComercioId(UUID comercioId);
}
