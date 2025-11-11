package com.example.retail.domain;

import javax.persistence.*;

import java.math.BigDecimal;

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

    public Utilisateur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Utilisateur f) {
        fournisseur = f;
    }

}