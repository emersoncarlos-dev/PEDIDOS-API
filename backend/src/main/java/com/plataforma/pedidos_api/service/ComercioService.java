package com.plataforma.pedidos_api.service;

import com.plataforma.pedidos_api.dto.ComercioRequest;
import com.plataforma.pedidos_api.entity.Comercio;
import com.plataforma.pedidos_api.exception.ConflitoException;
import com.plataforma.pedidos_api.repository.ComercioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComercioService {

    private final ComercioRepository repository;

    public ComercioService(ComercioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Comercio criar(ComercioRequest request) {
        if (repository.existsBySlug(request.slug())) {
            throw new ConflitoException("Já existe um comércio com o slug: " + request.slug());
        }

        Comercio comercio = new Comercio();
        comercio.setNome(request.nome());
        comercio.setSlug(request.slug());
        comercio.setTelefone(request.telefone());
        return repository.save(comercio);
    }
}