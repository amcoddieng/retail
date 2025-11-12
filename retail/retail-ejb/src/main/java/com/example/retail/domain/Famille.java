package com.example.retail.domain;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Famille {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nom;
    private String description;
    
    @ManyToMany(mappedBy = "familles")
    private List<Catalogue> catalogues = new ArrayList<>();

    // Méthodes utilitaires pour gérer la relation avec les catalogues
    public void addCatalogue(Catalogue catalogue) {
        if (!catalogues.contains(catalogue)) {
            catalogues.add(catalogue);
            if (!catalogue.getFamilles().contains(this)) {
                catalogue.getFamilles().add(this);
            }
        }
    }
    
    public void removeCatalogue(Catalogue catalogue) {
        if (catalogues.remove(catalogue)) {
            catalogue.getFamilles().remove(this);
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
    
    public List<Catalogue> getCatalogues() {
        return catalogues;
    }
    
    public void setCatalogues(List<Catalogue> catalogues) {
        this.catalogues = catalogues;
    }
}