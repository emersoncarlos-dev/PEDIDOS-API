package com.plataforma.pedidos_api.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarDisponibilidadeRequest(@NotNull Boolean disponivel) {}