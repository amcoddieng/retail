package com.example.retail.domain;

import javax.persistence.*;

import java.util.ArrayList;

@Entity
public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Commande commande;
    @Enumerated(EnumType.STRING)
    private LivraisonStatut statut = LivraisonStatut.PREPAREE;
    @OneToMany(mappedBy = "livraison", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<LivraisonLigne> lignes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande c) {
        commande = c;
    }

    public LivraisonStatut getStatut() {
        return statut;
    }

    public void setStatut(LivraisonStatut s) {
        statut = s;
    }

    public java.util.List<LivraisonLigne> getLignes() {
        return lignes;
    }
}