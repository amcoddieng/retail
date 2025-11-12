package com.example.retail.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String libelle;
    private Double prix;
    @Column(name = "stock_disponible")
    private Integer stockDisponible;
    @ManyToOne
    private Famille famille;
    @Lob
    @Column(name = "image")
    private byte[] image;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;
    
    @ManyToMany(mappedBy = "produits")
    private List<Boutique> boutiques = new ArrayList<>();
    
    @ManyToMany(mappedBy = "produits")
    private List<Catalogue> catalogues = new ArrayList<>();
    
    // Méthodes utilitaires pour gérer la relation bidirectionnelle
    public void addBoutique(Boutique boutique) {
        if (!boutiques.contains(boutique)) {
            boutiques.add(boutique);
            boutique.getProduits().add(this);
        }
    }
    
    public void removeBoutique(Boutique boutique) {
        if (boutiques.remove(boutique)) {
            boutique.getProduits().remove(this);
        }
    }


    public Long getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String s) {
        libelle = s;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double p) {
        prix = p;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer s) {
        stockDisponible = s;
    }

    public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getImageContentType() {
		return imageContentType;
	}

	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	public Famille getFamille() {
        return famille;
    }

    public void setFamille(Famille f) {
        famille = f;
    }

    @ManyToOne
    private Utilisateur fournisseur;

    public List<Boutique> getBoutiques() {
        return boutiques;
    }

    public void setBoutiques(List<Boutique> boutiques) {
        this.boutiques = boutiques;
    }
    
    public List<Catalogue> getCatalogues() {
        return catalogues;
    }
    
    public void setCatalogues(List<Catalogue> catalogues) {
        this.catalogues = catalogues;
    }
    
    // Méthodes utilitaires pour gérer la relation avec les catalogues
    public void addCatalogue(Catalogue catalogue) {
        if (!catalogues.contains(catalogue)) {
            catalogues.add(catalogue);
            if (!catalogue.getProduits().contains(this)) {
                catalogue.getProduits().add(this);
            }
        }
    }
    
    public void removeCatalogue(Catalogue catalogue) {
        if (catalogues.remove(catalogue)) {
            catalogue.getProduits().remove(this);
        }
    }

    public Utilisateur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Utilisateur f) {
        fournisseur = f;
    }

}