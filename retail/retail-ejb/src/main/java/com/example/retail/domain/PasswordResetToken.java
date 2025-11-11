package com.example.retail.domain;

import javax.persistence.*;

import java.time.Instant;

@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Utilisateur utilisateur;
    @Column(nullable = false, unique = true)
    private String token;
    private Instant expiresAt;

    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur u) {
        utilisateur = u;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String t) {
        token = t;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant i) {
        expiresAt = i;
    }
}