package com.plataforma.pedidos_api.controller;

import com.plataforma.pedidos_api.dto.AtualizarStatusRequest;
import com.plataforma.pedidos_api.dto.PedidoRequest;
import com.plataforma.pedidos_api.dto.PedidoResponse;
import com.plataforma.pedidos_api.entity.Pedido;
import com.plataforma.pedidos_api.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> criar(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = pedidoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/comercio/{comercioId}")
    public List<PedidoResponse> listarPorComercio(@PathVariable UUID comercioId) {
        return pedidoService.listarPorComercio(comercioId);
    }

    @PatchMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable UUID id,
                                  @Valid @RequestBody AtualizarStatusRequest request) {
        return pedidoService.atualizarStatus(id, request);
    }
}