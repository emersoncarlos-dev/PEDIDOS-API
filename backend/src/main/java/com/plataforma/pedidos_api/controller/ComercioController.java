package com.plataforma.pedidos_api.controller;

import com.plataforma.pedidos_api.entity.Comercio;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comercios")
public class ComercioController {

    private final ComercioRepository repository;

    public ComercioController(ComercioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Comercio> listar() {
        return repository.findAll();
    }
}