package com.plataforma.pedidos_api.repository;

import com.plataforma.pedidos_api.entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, UUID> {
    List<ItemPedido> findByPedidoId(UUID pedidoId);
}