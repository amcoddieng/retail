package com.example.retail.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Boutique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(length = 512)
    private String adresse;
    
    @Column(length = 20)
    private String telephone;
    
    @Column(length = 100)
    private String email;
    
    @Column(name = "code_postal", length = 10)
    private String codePostal;
    
    private boolean active = true;
    
    @ManyToOne
    @JoinColumn(name = "gerant_id")
    private Utilisateur gerant;
    
    @ManyToMany
    @JoinTable(
        name = "boutique_caissiers",
        joinColumns = @JoinColumn(name = "boutique_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private Set<Utilisateur> caissiers = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "boutique_produits",
        joinColumns = @JoinColumn(name = "boutique_id"),
        inverseJoinColumns = @JoinColumn(name = "produit_id")
    )
    private Set<Produit> produits = new HashSet<>();
    
    // Méthodes utilitaires pour gérer la relation avec les caissiers
    public void addCaissier(Utilisateur caissier) {
        if (caissier != null && !caissiers.contains(caissier)) {
            caissiers.add(caissier);
            caissier.getBoutiquesEnTantQueCaissier().add(this);
        }
    }
    
    public void removeCaissier(Utilisateur caissier) {
        if (caissier != null && caissiers.remove(caissier)) {
            caissier.getBoutiquesEnTantQueCaissier().remove(this);
        }
    }
    
    // Méthodes utilitaires pour gérer la relation avec les produits
    public void addProduit(Produit produit) {
        if (produit != null && !produits.contains(produit)) {
            produits.add(produit);
            produit.getBoutiques().add(this);
        }
    }
    
    public void removeProduit(Produit produit) {
        if (produit != null && produits.remove(produit)) {
            produit.getBoutiques().remove(this);
        }
    }
    
    // Getters et Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCodePostal() {
        return codePostal;
    }
    
    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Utilisateur getGerant() {
        return gerant;
    }
    
    public void setGerant(Utilisateur gerant) {
        this.gerant = gerant;
    }
    
    public Set<Utilisateur> getCaissiers() {
        return caissiers;
    }
    
    public void setCaissiers(Set<Utilisateur> caissiers) {
        this.caissiers = caissiers;
    }
    
    
    public Set<Produit> getProduits() {
        return produits;
    }
    
    public void setProduits(Set<Produit> produits) {
        this.produits = produits;
    }
    
}
