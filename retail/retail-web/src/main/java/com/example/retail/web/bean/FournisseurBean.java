package com.example.retail.web.bean;

import com.example.retail.domain.Commande;
import com.example.retail.domain.LotAppro;
import com.example.retail.domain.LotItem;
import com.example.retail.domain.Produit;
import com.example.retail.domain.Utilisateur;
import com.example.retail.service.AdminService;
import com.example.retail.service.FournisseurService;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Named
@SessionScoped
public class FournisseurBean implements Serializable {

    @EJB
    private FournisseurService svc;

    @EJB
    private AdminService adminService;

    private Long lotId;
    private Long produitId;
    private Integer quantite;
    private List<Utilisateur> fournisseurs;
    private List<Produit> mesProduits;
    private LotAppro lotSelectionne;
    


    @PostConstruct
    public void init() {
        System.out.println(">>> Initialisation de FournisseurBean");

        // Initialiser les listes vides pour éviter les NPE
        this.fournisseurs = new ArrayList<>();
        this.mesProduits = new ArrayList<>();

        // Charger les données de façon sécurisée
        try {
            System.out.println(">>> Chargement des fournisseurs...");
            List<Utilisateur> loadedFournisseurs = svc.listerFournisseurs();
            if (loadedFournisseurs != null) {
                this.fournisseurs = loadedFournisseurs;
                System.out.println(">>> " + fournisseurs.size() + " fournisseurs chargés");
            }
        } catch (Exception e) {
            System.err.println(">>> ERREUR chargement fournisseurs: " + e.getMessage());
            this.fournisseurs = new ArrayList<>(); // Garantir une liste non-null
        }

        try {
            System.out.println(">>> Chargement des produits...");
            List<Produit> loadedProduits = adminService.produits();
            if (loadedProduits != null) {
                this.mesProduits = loadedProduits;
                System.out.println(">>> " + mesProduits.size() + " produits chargés");
            }
        } catch (Exception e) {
            System.err.println(">>> ERREUR chargement produits: " + e.getMessage());
            this.mesProduits = new ArrayList<>(); // Garantir une liste non-null
        }

        System.out.println(">>> FournisseurBean initialisé avec succès");
    }

    // Getters sécurisés
    public List<Utilisateur> getFournisseurs() {
        if (fournisseurs == null) {
            System.out.println(">>> WARNING: fournisseurs était null, initialisation");
            fournisseurs = new ArrayList<>();
        }
        return fournisseurs;
    }

    public List<Produit> getMesProduits() {
        if (mesProduits == null) {
            System.out.println(">>> WARNING: mesProduits était null, initialisation");
            mesProduits = new ArrayList<>();
        }
        return mesProduits;
    }

    public List<LotAppro> getMesLots() {

        try {
            List<LotAppro> lots = svc.lotsFournisseur(getCurrentUserLogin());
            System.out.println(">>> Lots: "+ lots.get(0).getItems());
            return Optional.of(lots).orElseGet(ArrayList::new);
        } catch (Exception e) {
            System.err.println(">>> ERREUR getMesLots: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public Integer getQuantiteTotaleLot(LotAppro lot) {
        if (lot == null) {
            return 0;
        }
        try {
            return Optional.ofNullable(lot.getItems())
                    .orElseGet(ArrayList::new)
                    .stream()
                    .mapToInt(item -> item != null ? item.getQuantite() : 0)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }

    // Méthodes d'action
    public void creerLot() {
        try {


            this.lotId = svc.creerLot(getCurrentUserLogin());
            addMessage("Lot #" + lotId + " créé pour " + getCurrentUserLogin());

        } catch (Exception e) {
            addErrorMessage("Erreur création: " + e.getMessage());
        }
    }

    public void ajouterItem() {
        try {
            if (lotId == null) {
                addErrorMessage("Veuillez créer un lot d'abord");
                return;
            }
            if (produitId == null || quantite == null || quantite <= 0) {
                addErrorMessage("Produit et quantité requis");
                return;
            }

            svc.ajouterItem(lotId, produitId, quantite);
            addMessage("Article ajouté au lot #" + lotId);

            // Reset des champs
            this.produitId = null;
            this.quantite = null;

        } catch (Exception e) {
            addErrorMessage("Erreur ajout: " + e.getMessage());
        }
    }
    
    public void toggleLotDetail(LotAppro lot) {
        if (lotSelectionne != null && lotSelectionne.getId().equals(lot.getId())) {
            // Si on clique sur le même lot, on masque les détails
            lotSelectionne = null;
        } else {
            // Sinon on affiche les détails du nouveau lot
            lotSelectionne = lot;
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

    private void addMessage(String message) {
        try {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new javax.faces.application.FacesMessage(
                            javax.faces.application.FacesMessage.SEVERITY_INFO, message, ""));
        } catch (Exception e) {
            System.err.println("Erreur addMessage: " + e.getMessage());
        }
    }

    private void addErrorMessage(String message) {
        try {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new javax.faces.application.FacesMessage(
                            javax.faces.application.FacesMessage.SEVERITY_ERROR, message, ""));
        } catch (Exception e) {
            System.err.println("Erreur addErrorMessage: " + e.getMessage());
        }
    }

    // Getters et Setters

    // Getters/Setters
    public Long getLotId() { return lotId; }
    public void setLotId(Long lotId) { this.lotId = lotId; }
    public Long getProduitId() { return produitId; }
    public void setProduitId(Long produitId) { this.produitId = produitId; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public LotAppro getLotSelectionne() { return lotSelectionne; }
    public void setLotSelectionne(LotAppro lotSelectionne) { this.lotSelectionne = lotSelectionne; }
}