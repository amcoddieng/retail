package com.example.retail.domain;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

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
    
    @ManyToMany
    @JoinTable(name = "catalogue_famille",
            joinColumns = @JoinColumn(name = "catalogue_id"),
            inverseJoinColumns = @JoinColumn(name = "famille_id"))
    private List<Famille> familles = new ArrayList<>();
    
    // Méthodes utilitaires pour gérer la relation avec les familles
    public void addFamille(Famille famille) {
        if (!familles.contains(famille)) {
            familles.add(famille);
            if (!famille.getCatalogues().contains(this)) {
                famille.getCatalogues().add(this);
            }
        }
    }
    
    public void removeFamille(Famille famille) {
        if (familles.remove(famille)) {
            famille.getCatalogues().remove(this);
        }
    }

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
    
    public List<Famille> getFamilles() {
        return familles;
    }
    
    public void setFamilles(List<Famille> familles) {
        this.familles = familles;
    }
}