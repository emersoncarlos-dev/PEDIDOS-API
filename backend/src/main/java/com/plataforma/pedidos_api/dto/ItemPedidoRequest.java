package com.plataforma.pedidos_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemPedidoRequest(
        @NotNull UUID produtoId,
        @NotNull @Positive Integer quantidade
) {}
