package com.plataforma.pedidos_api.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarStatusRequest(@NotBlank String status) {}