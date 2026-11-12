package com.plataforma.pedidos_api.controller;

import com.plataforma.pedidos_api.dto.ComercioRequest;
import com.plataforma.pedidos_api.entity.Comercio;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import com.plataforma.pedidos_api.service.ComercioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comercios")
public class ComercioController {

    private final ComercioRepository repository;
    private final ComercioService service;

    public ComercioController(ComercioRepository repository, ComercioService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public List<Comercio> listar() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Comercio> criar(@Valid @RequestBody ComercioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }
}