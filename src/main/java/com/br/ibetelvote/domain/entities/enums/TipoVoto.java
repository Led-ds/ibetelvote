package com.br.ibetelvote.domain.entities.enums;

public enum TipoVoto {
    CANDIDATO("Voto em candidato"),
    BRANCO("Voto em branco"),
    NULO("Voto nulo");

    private final String descricao;

    TipoVoto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
