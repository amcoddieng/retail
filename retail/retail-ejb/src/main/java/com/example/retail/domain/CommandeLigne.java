package com.example.retail.domain;

import javax.persistence.*;

import java.math.BigDecimal;

@Entity(name = "commande_ligne")
public class CommandeLigne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Commande commande;
    @ManyToOne(optional = false)
    private Produit produit;
    private Integer quantite;
    @Column(name = "prix_unitaire")
    private Double prixUnitaire;

    public Long getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande c) {
        commande = c;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit p) {
        produit = p;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer q) {
        quantite = q;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Double p) {
        prixUnitaire = p;
    }

}