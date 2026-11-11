package com.plataforma.pedidos_api.entity;

public enum StatusPedido {
    RECEBIDO("recebido"),
    EM_PREPARO("em_preparo"),
    PRONTO("pronto"),
    ENTREGUE("entregue"),
    CANCELADO("cancelado");

    private final String valor;

    StatusPedido(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public boolean finalizado() {
        return this == ENTREGUE || this == CANCELADO;
    }

    public static StatusPedido deValor(String valor) {
        for (StatusPedido status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status inválido: " + valor);
    }
}