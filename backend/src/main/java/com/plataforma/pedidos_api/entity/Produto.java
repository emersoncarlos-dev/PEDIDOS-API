package com.plataforma.pedidos_api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "comercio_id", nullable = false)
    private UUID comercioId;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean disponivel = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criado_em = OffsetDateTime.now();

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getComercioId() { return comercioId; }
    public void setComercioId(UUID comercioId) { this.comercioId = comercioId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    public OffsetDateTime getCriado_em() { return criado_em; }
    public void setCriado_em(OffsetDateTime criado_em) { this.criado_em = criado_em; }
}
