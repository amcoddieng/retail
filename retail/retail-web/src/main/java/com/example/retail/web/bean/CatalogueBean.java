package com.example.retail.web.bean;

import com.example.retail.domain.Commande;
import com.example.retail.domain.Produit;
import com.example.retail.service.CommandeService;
import com.example.retail.service.CatalogueService;
import com.example.retail.domain.Catalogue;
import com.example.retail.domain.Famille;
import com.example.retail.service.AdminService;
import com.example.retail.web.aop.Auditable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@SessionScoped
public class CatalogueBean implements Serializable {
    @EJB
    private CommandeService service;
    
    @EJB
    private CatalogueService catalogueService;
    
    @EJB
    private AdminService adminService;
    
    @Inject
    private LoginBean loginBean;
    
    // Pour la gestion du panier
    private List<Produit> produits;
    private Map<Long, Integer> panier = new LinkedHashMap<>();
    
    // Pour la gestion des catalogues et familles
    private List<Catalogue> catalogues;
    private Catalogue selectedCatalogue;
    private List<Famille> famillesDisponibles;
    private List<Famille> famillesSelectionnees;
    private Long familleId;
    private Famille familleASupprimer;

    @PostConstruct
    public void init() {
        produits = service.listerProduits();
        catalogues = catalogueService.listerCatalogues();
        chargerFamillesDisponibles();
        System.out.println("CatalogueBean initialisé avec " + produits.size() + " produits et " + catalogues.size() + " catalogues");
    }
    
    private void chargerFamillesDisponibles() {
        if (selectedCatalogue != null) {
            // Charger les familles disponibles pour le catalogue sélectionné
            famillesDisponibles = adminService.familles().stream()
                    .filter(f -> !selectedCatalogue.getFamilles().contains(f))
                    .collect(Collectors.toList());
        } else {
            // Si aucun catalogue n'est sélectionné, charger toutes les familles
            famillesDisponibles = adminService.familles();
        }
    }

    // Méthodes pour la gestion du panier
    public void ajouter(Long pid) {
        if (!loginBean.isUserLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Connexion requise", "Veuillez vous connecter pour ajouter des articles au panier"));
            loginBean.redirectToLogin();
            return;
        }
        System.out.println("Ajouter produit: " + pid);
        Integer quantite = panier.get(pid);
        panier.put(pid, quantite == null ? 1 : quantite + 1);
        System.out.println("Panier après ajout: " + panier);
    }
    
    // Méthodes pour la gestion des catalogues et familles
    public void onCatalogueSelect() {
        if (selectedCatalogue != null) {
            // Charger les familles du catalogue sélectionné
            famillesSelectionnees = new ArrayList<>(selectedCatalogue.getFamilles());
            chargerFamillesDisponibles();
        }
    }
    
    public void ajouterFamille() {
        if (selectedCatalogue != null && familleId != null) {
            catalogueService.ajouterFamilleAuCatalogue(selectedCatalogue.getId(), familleId);
            // Mettre à jour les listes
            selectedCatalogue = catalogueService.trouverCatalogueParId(selectedCatalogue.getId());
            onCatalogueSelect();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Famille ajoutée", "La famille a été ajoutée au catalogue avec succès"));
        }
    }
    
    public void retirerFamille() {
        if (selectedCatalogue != null && familleASupprimer != null) {
            catalogueService.retirerFamilleDuCatalogue(selectedCatalogue.getId(), familleASupprimer.getId());
            // Mettre à jour les listes
            selectedCatalogue = catalogueService.trouverCatalogueParId(selectedCatalogue.getId());
            onCatalogueSelect();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Famille retirée", "La famille a été retirée du catalogue avec succès"));
        }
    }
    
    public void retirerFamille(Famille famille) {
        this.familleASupprimer = famille;
        retirerFamille();
    }
    
    // Getters et setters
    public List<Catalogue> getCatalogues() {
        return catalogues;
    }

    public void setCatalogues(List<Catalogue> catalogues) {
        this.catalogues = catalogues;
    }

    public Catalogue getSelectedCatalogue() {
        return selectedCatalogue;
    }

    public void setSelectedCatalogue(Catalogue selectedCatalogue) {
        this.selectedCatalogue = selectedCatalogue;
        if (selectedCatalogue != null) {
            onCatalogueSelect();
        }
    }

    public List<Famille> getFamillesDisponibles() {
        return famillesDisponibles;
    }

    public void setFamillesDisponibles(List<Famille> famillesDisponibles) {
        this.famillesDisponibles = famillesDisponibles;
    }

    public List<Famille> getFamillesSelectionnees() {
        return famillesSelectionnees;
    }

    public void setFamillesSelectionnees(List<Famille> famillesSelectionnees) {
        this.famillesSelectionnees = famillesSelectionnees;
    }

    public Long getFamilleId() {
        return familleId;
    }

    public void setFamilleId(Long familleId) {
        this.familleId = familleId;
    }
    
    public Famille getFamilleASupprimer() {
        return familleASupprimer;
    }
    
    public void setFamilleASupprimer(Famille familleASupprimer) {
        this.familleASupprimer = familleASupprimer;
    }

    public void inc(Long pid) {
        System.out.println("Incrémenter produit: " + pid);
        Integer quantite = panier.get(pid);
        if (quantite != null) {
            panier.put(pid, quantite + 1);
            System.out.println("Nouvelle quantité: " + panier.get(pid));
        }
    }

    public void dec(Long pid) {
        System.out.println("Décrémenter produit: " + pid);
        Integer quantite = panier.get(pid);
        if (quantite != null) {
            if (quantite > 1) {
                panier.put(pid, quantite - 1);
                System.out.println("Nouvelle quantité: " + panier.get(pid));
            } else {
                panier.remove(pid);
                System.out.println("Produit retiré du panier");
            }
        }
    }

    public void remove(Long pid) {
        System.out.println("Retirer produit: " + pid);
        panier.remove(pid);
        System.out.println("Panier après retrait: " + panier);
    }

    public List<CartLine> getCartLines() {
        List<CartLine> list = new ArrayList<>();
        System.out.println("Génération des lignes du panier. Panier: " + panier);

        for (Map.Entry<Long, Integer> entry : panier.entrySet()) {
            Long produitId = entry.getKey();
            Integer quantite = entry.getValue();

            Produit produit = produits.stream()
                    .filter(p -> p.getId().equals(produitId))
                    .findFirst()
                    .orElse(null);

            if (produit != null && quantite != null) {
                list.add(new CartLine(produit.getId(), produit.getLibelle(), produit.getPrix(), quantite));
            }
        }
        System.out.println("Lignes générées: " + list.size());
        return list;
    }

    public Double getTotal() {
        double total = 0.0;
        for (CartLine item : getCartLines()) {
            total += item.getLigneTotal();
        }
        return total;
    }

    @Auditable("passer-commande")
    public void commander() {
        try {
            System.out.println("Tentative de commande. Panier: " + panier);

            if (panier.isEmpty()) {
                addMessage("Panier vide", FacesMessage.SEVERITY_WARN);
                return;
            }

            // Utiliser un login par défaut temporairement
            String login = getCurrentUserLogin();

            Commande c = service.passerCommande(login, panier);
            panier.clear();

            addMessage("Commande #" + c.getId() + " créée (total: " + c.getTotal() + " FCFA)", FacesMessage.SEVERITY_INFO);
            System.out.println("Commande créée avec succès: " + c.getId());

        } catch (Exception ex) {
            System.err.println("Erreur lors de la commande: " + ex.getMessage());
            ex.printStackTrace();
            addMessage("Erreur lors de la commande: " + ex.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    private String getCurrentUserLogin() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                if (session != null) {
                    return (String) session.getAttribute("username");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur getCurrentUserLogin: " + e.getMessage());
        }
        return null;
    }


    private void addMessage(String message, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, ""));
    }

    // Getters
    public List<Produit> getProduits() { return produits; }
    public Map<Long, Integer> getPanier() { return panier; }

    public static class CartLine {
        private Long produitId;
        private String libelle;
        private Double prixUnitaire;
        private int quantite;

        public CartLine(Long id, String lib, Double prix, int q) {
            this.produitId = id;
            this.libelle = lib;
            this.prixUnitaire = prix;
            this.quantite = q;
        }

        public Long getProduitId() { return produitId; }
        public String getLibelle() { return libelle; }
        public Double getPrixUnitaire() { return prixUnitaire; }
        public int getQuantite() { return quantite; }
        public Double getLigneTotal() { return prixUnitaire * quantite; }
    }
}