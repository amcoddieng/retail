package com.example.retail.service.dto;

public class PickItem {
    public Long produitId;
    public String libelle;
    public Integer quantite;

    public PickItem(Long id, String lib, Integer q) {
        this.produitId = id;
        this.libelle = lib;
        this.quantite = q;
    }

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}