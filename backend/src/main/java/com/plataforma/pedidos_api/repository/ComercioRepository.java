package com.plataforma.pedidos_api.repository;

import com.plataforma.pedidos_api.entity.Comercio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ComercioRepository extends JpaRepository<Comercio, UUID> {

    Optional<Comercio> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
