package com.example.retail.domain;

import javax.persistence.*;

@Entity(name = "livraison_ligne")
public class LivraisonLigne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Livraison livraison;
    @ManyToOne(optional = false)
    private Produit produit;
    private Integer quantite;

    public Long getId() {
        return id;
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison l) {
        livraison = l;
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
}