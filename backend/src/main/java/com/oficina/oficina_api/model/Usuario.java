package com.oficina.oficina_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "senha", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "bloqueado", nullable = false)
    private boolean bloqueado = false;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "funcao_id", nullable = false)
    private Funcao funcao;

    public Usuario() {}

    public Usuario(String email, String senhaHash, Funcao funcao) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.funcao = funcao;
        this.ativo = true;
        this.bloqueado = false;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getLogin() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public boolean isAtivo() { return ativo; }
    public boolean isBloqueado() { return bloqueado; }
    public Funcao getFuncao() { return funcao; }
    public Role getRole() { return Role.valueOf(funcao.getNome().replace("ROLE_", "")); }
}
