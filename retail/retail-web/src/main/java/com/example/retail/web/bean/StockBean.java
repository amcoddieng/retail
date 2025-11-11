package com.example.retail.web.bean;

import com.example.retail.domain.Produit;
import com.example.retail.service.StockService;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Named("stockBean")
@SessionScoped
public class StockBean implements Serializable {

    @EJB
    private StockService stock;

    private Long produitId;
    private Integer quantite = 1;
    private String lotsJson = "{}";

    public List<Produit> getProduits() {
        // liste pour table & select
        return stock.listProduits();
    }

    public void ajouter() {
        if (produitId == null || (quantite == null || quantite <= 0)) {
            err("Sélectionne un produit et une quantité > 0.");
            return;
        }
        try {
            stock.ajouter(produitId, quantite);
            msg("Stock ajouté.");
            // reset minimal
            quantite = 1;
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    private List<LotItem> lotItems = new ArrayList<>();

    // Classe interne pour représenter une ligne produit/quantité
    public static class LotItem {
        private Long produitId;
        private Integer quantite;

        public LotItem() {
            // Constructeur par défaut
        }

        public LotItem(Long produitId, Integer quantite) {
            this.produitId = produitId;
            this.quantite = quantite;
        }

        // Getters et setters
        public Long getProduitId() { return produitId; }
        public void setProduitId(Long produitId) { this.produitId = produitId; }

        public Integer getQuantite() { return quantite; }
        public void setQuantite(Integer quantite) { this.quantite = quantite; }
    }

    // Getter et setter pour la liste
    public List<LotItem> getLotItems() {
        return lotItems;
    }

    public void setLotItems(List<LotItem> lotItems) {
        this.lotItems = lotItems;
    }

    // Méthode pour ajouter une ligne vide
    public void ajouterLigne() {
        if (lotItems == null) {
            lotItems = new ArrayList<>();
        }
        lotItems.add(new LotItem());
    }

    // Méthode pour supprimer une ligne
    public void supprimerLigne(LotItem item) {
        if (lotItems != null) {
            lotItems.remove(item);
        }
    }

    // Méthode pour appliquer les modifications
    public void fournir() {
        try {
            if (lotItems == null || lotItems.isEmpty()) {
                err("Veuillez ajouter au moins une ligne");
                return;
            }

            Map<Long, Integer> lots = new LinkedHashMap<>();
            int added = 0;
            int removed = 0;

            for (LotItem item : lotItems) {
                if (item.getProduitId() != null && item.getQuantite() != null) {
                    lots.put(item.getProduitId(), item.getQuantite());
                    if (item.getQuantite() > 0) {
                        added++;
                    } else if (item.getQuantite() < 0) {
                        removed++;
                    }
                }
            }

            if (lots.isEmpty()) {
                err("Aucune donnée valide à traiter");
                return;
            }

            stock.fournir(lots);

            String message = "Opération effectuée : ";
            if (added > 0) message += added + " ajout(s) ";
            if (removed > 0) message += (added > 0 ? "et " : "") + removed + " retrait(s)";
            msg(message);

            // Réinitialiser la liste après traitement
            lotItems = new ArrayList<>();

        } catch (Exception ex) {
            err("Erreur lors de l'application : " + ex.getMessage());
        }
    }

    // Méthode pour vider toutes les lignes
    public void viderTout() {
        lotItems = new ArrayList<>();
    }

    /* ---------- utils messages ---------- */
    private void msg(String m) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, ""));
    }
    private void err(String m) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, ""));
    }

    /* ---------- getters/setters ---------- */
    public Long getProduitId() { return produitId; }
    public void setProduitId(Long v) { produitId = v; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer v) { quantite = v; }

    public String getLotsJson() { return lotsJson; }
    public void setLotsJson(String s) { lotsJson = s; }
}
