package com.plataforma.pedidos_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PedidoRequest(
        @NotNull UUID comercioId,
        @NotBlank String nomeCliente,
        String telefoneCliente,
        @NotEmpty @Valid List<ItemPedidoRequest> itens
) {}