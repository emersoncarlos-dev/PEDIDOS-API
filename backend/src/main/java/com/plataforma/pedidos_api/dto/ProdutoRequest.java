package com.plataforma.pedidos_api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoRequest(
        @NotNull UUID comercioId,
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 500) String descricao,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal preco
) {}