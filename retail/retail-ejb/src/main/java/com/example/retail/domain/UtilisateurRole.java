package com.example.retail.domain;

import javax.persistence.*;

@Entity
@Table(name = "utilisateur_role")
public class UtilisateurRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur u) {
        utilisateur = u;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role r) {
        role = r;
    }
}