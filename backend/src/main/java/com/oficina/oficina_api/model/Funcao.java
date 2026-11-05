package com.oficina.oficina_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "funcao")
public class Funcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 30)
    private String nome;

    // Construtor exigido pelo JPA
    protected Funcao() {
    }

    // Construtor para criar funções pelo código
    public Funcao(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
