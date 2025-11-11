package com.example.retail.domain;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Catalogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nom;
    private String description;
    @ManyToMany
    @JoinTable(name = "catalogue_produit",
            joinColumns = @JoinColumn(name = "catalogue_id"),
            inverseJoinColumns = @JoinColumn(name = "produit_id"))
    private List<Produit> produits = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String s) {
        nom = s;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String s) {
        description = s;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> p) {
        produits = p;
    }
}