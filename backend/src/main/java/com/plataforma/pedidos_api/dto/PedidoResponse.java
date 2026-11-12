package com.plataforma.pedidos_api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        UUID comercioId,
        String nomeCliente,
        String telefoneCliente,
        String status,
        BigDecimal total,
        OffsetDateTime criadoEm,
        List<ItemPedidoResponse> itens
) {}