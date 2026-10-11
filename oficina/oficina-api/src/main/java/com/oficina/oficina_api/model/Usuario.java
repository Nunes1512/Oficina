package com.oficina.oficina_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean ativo = true;

    public Usuario() {}

    public Usuario(String nome, String login, String senhaHash, Role role, boolean ativo) {
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.role = role;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public String getSenhaHash() { return senhaHash; }
    public Role getRole() { return role; }
    public boolean isAtivo() { return ativo; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setLogin(String login) { this.login = login; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public void setRole(Role role) { this.role = role; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
