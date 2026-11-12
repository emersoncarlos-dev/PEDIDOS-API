package com.plataforma.pedidos_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ComercioRequest(
        @NotBlank String nome,
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                 message = "deve conter apenas letras minúsculas, números e hífens")
        String slug,
        String telefone
) {}