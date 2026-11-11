package com.plataforma.pedidos_api.repository;

import com.plataforma.pedidos_api.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    List<Pedido> findByComercioId(UUID comercioId);
    List<Pedido> findByComercioIdOrderByCriadoEmDesc(UUID comercioId);
}
