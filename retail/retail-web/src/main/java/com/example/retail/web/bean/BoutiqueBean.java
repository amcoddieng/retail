package com.example.retail.web.bean;

import com.example.retail.domain.Boutique;
import com.example.retail.domain.Produit;
import com.example.retail.domain.Utilisateur;
import com.example.retail.service.AuthService;
import com.example.retail.service.BoutiqueService;
import com.example.retail.service.BusinessException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("boutiqueBean")
@ViewScoped
public class BoutiqueBean implements Serializable {

    @EJB
    private BoutiqueService boutiqueService;
    
    @EJB
    private AuthService authService;
    
    @Inject
    private LoginBean loginBean;
    
    // Données de la boutique en cours de création/modification
    private Boutique boutique = new Boutique();
    private Long selectedBoutiqueId;
    
    // Pour la gestion des caissiers
    private Long selectedCaissierId;
    private List<Utilisateur> availableCaissiers = new ArrayList<>();
    
    // Pour la gestion des produits
    private Long selectedProduitId;
    private List<Produit> availableProduits = new ArrayList<>();
    
    // Liste des boutiques
    private List<Boutique> boutiques = new ArrayList<>();
    
    @PersistenceContext
    private transient EntityManager em;
    
    @PostConstruct
    public void init() {
        // Cette méthode est maintenant gérée par chargerBoutique via f:viewParam
        chargerBoutiques();
    }
    
    public void chargerBoutiques() {
        // Charger les boutiques en fonction du rôle de l'utilisateur
        Utilisateur currentUser = loginBean.getLoggedInUser();
        if (currentUser != null) {
            if (isAdmin()) {
                boutiques = boutiqueService.listerToutesLesBoutiques();
            } else if (isGerant()) {
                boutiques = boutiqueService.listerBoutiquesParGerant(currentUser);
            }
        }
        
        // Charger les caissiers disponibles
        availableCaissiers = authService.listUsers().stream()
                .filter(u -> {
                    List<String> roles = authService.rolesOf(u.getLogin());
                    return roles.contains("CAISSIER");
                })
                .collect(Collectors.toList());
        
        // Charger les produits disponibles
        availableProduits = em.createQuery("SELECT p FROM Produit p", Produit.class).getResultList();
    }
    
    public void chargerBoutique() {
        if (selectedBoutiqueId != null) {
            boutique = boutiqueService.trouverParId(selectedBoutiqueId);
            if (boutique == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Boutique introuvable");
                return;
            }
            
            // Vérifier les droits d'accès
            if (!peutModifierBoutique(boutique)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous n'avez pas les droits pour modifier cette boutique");
                return;
            }
        } else {
            // Nouvelle boutique
            boutique = new Boutique();
            boutique.setActive(true);
            
            // Si l'utilisateur est un gérant, il est automatiquement défini comme gérant de la nouvelle boutique
            if (isGerant()) {
                boutique.setGerant(loginBean.getLoggedInUser());
            }
        }
    }
    
    private boolean peutModifierBoutique(Boutique b) {
        if (b == null) return false;
        if (isAdmin()) return true;
        
        // Un gérant ne peut modifier que ses propres boutiques
        if (isGerant() && b.getGerant() != null) {
            return b.getGerant().getId().equals(loginBean.getLoggedInUser().getId());
        }
        
        return false;
    }
    
    private boolean isAdmin() {
        return loginBean.getLoggedInUser() != null && 
               loginBean.getLoggedInUser().getRoles() != null &&
               loginBean.getLoggedInUser().getRoles().stream()
                   .anyMatch(r -> "ADMIN".equals(r.getCode()));
    }
    
    private boolean isGerant() {
        return loginBean.getLoggedInUser() != null && 
               loginBean.getLoggedInUser().getRoles() != null &&
               loginBean.getLoggedInUser().getRoles().stream()
                   .anyMatch(r -> "GERANT".equals(r.getCode()));
    }
    
    public String enregistrer() {
        try {
            // Validation des champs obligatoires
            if (boutique.getNom() == null || boutique.getNom().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le nom de la boutique est obligatoire");
                return null;
            }
            
            if (boutique.getAdresse() == null || boutique.getAdresse().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "L'adresse de la boutique est obligatoire");
                return null;
            }
            
            if (boutique.getTelephone() == null || boutique.getTelephone().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le téléphone de la boutique est obligatoire");
                return null;
            }
            
            // Vérification des droits
            if (boutique.getId() != null && !peutModifierBoutique(boutique)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous n'avez pas les droits pour modifier cette boutique");
                return null;
            }
            
            if (boutique.getId() == null) {
                // Création
                boutiqueService.creerBoutique(boutique, loginBean.getLoggedInUser());
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "La boutique a été créée avec succès");
            } else {
                // Mise à jour
                boutique = boutiqueService.mettreAJourBoutique(boutique);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "La boutique a été mise à jour avec succès");
            }
            
            return "/admin/boutiques/liste?faces-redirect=true";
            
        } catch (BusinessException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Erreur", "Une erreur inattendue est survenue");
            e.printStackTrace();
            return null;
        }
    }
    
    public void supprimer() {
        if (boutique != null && boutique.getId() != null) {
            try {
                boutiqueService.supprimerBoutique(boutique.getId());
                addMessage("Succès", "La boutique a été supprimée avec succès.");
            } catch (BusinessException e) {
                addMessage("Erreur", e.getMessage());
            }
        }
    }
    
    public void ajouterCaissier() {
        if (selectedCaissierId != null && boutique != null) {
            try {
                Utilisateur caissier = authService.trouverUtilisateurParId(selectedCaissierId);
                if (caissier != null) {
                    boutiqueService.ajouterCaissierABoutique(boutique.getId(), caissier);
                    addMessage("Succès", "Le caissier a été ajouté à la boutique.");
                    // Recharger les données
                    chargerBoutique();
                }
            } catch (BusinessException e) {
                addMessage("Erreur", e.getMessage());
            }
        }
    }
    
    public void retirerCaissier(Utilisateur caissier) {
        if (boutique != null && caissier != null) {
            try {
                boutiqueService.retirerCaissierDeBoutique(boutique.getId(), caissier);
                addMessage("Succès", "Le caissier a été retiré de la boutique.");
                // Recharger les données
                chargerBoutique();
            } catch (BusinessException e) {
                addMessage("Erreur", e.getMessage());
            }
        }
    }
    
    public void ajouterProduit() {
        if (selectedProduitId != null && boutique != null) {
            try {
                // Ici, vous devrez récupérer le produit à partir de son ID
                // Produit produit = produitService.trouverParId(selectedProduitId);
                // if (produit != null) {
                //     boutiqueService.ajouterProduitABoutique(boutique.getId(), produit);
                //     addMessage("Succès", "Le produit a été ajouté à la boutique.");
                //     // Recharger les données
                //     chargerBoutique();
                // }
            } catch (Exception e) {
                addMessage("Erreur", "Une erreur est survenue lors de l'ajout du produit.");
            }
        }
    }
    
    public void retirerProduit(Produit produit) {
        if (boutique != null && produit != null) {
            try {
                boutiqueService.retirerProduitDeBoutique(boutique.getId(), produit.getId());
                addMessage("Succès", "Le produit a été retiré de la boutique.");
                // Recharger les données
                chargerBoutique();
            } catch (BusinessException e) {
                addMessage("Erreur", e.getMessage());
            }
        }
    }
    
    // Méthode utilitaire pour afficher des messages
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    // Méthode utilitaire pour compatibilité avec l'ancien code
    private void addMessage(String summary, String detail) {
        addMessage(FacesMessage.SEVERITY_INFO, summary, detail);
    }
    
    // Getters et Setters
    
    public Boutique getBoutique() {
        return boutique;
    }
    
    public void setBoutique(Boutique boutique) {
        this.boutique = boutique;
    }
    
    public Long getSelectedBoutiqueId() {
        return selectedBoutiqueId;
    }
    
    public void setSelectedBoutiqueId(Long selectedBoutiqueId) {
        this.selectedBoutiqueId = selectedBoutiqueId;
        if (selectedBoutiqueId != null) {
            this.boutique = boutiqueService.trouverParId(selectedBoutiqueId);
        }
    }
    
    public List<Boutique> getBoutiques() {
        return boutiques;
    }
    
    public Long getSelectedCaissierId() {
        return selectedCaissierId;
    }
    
    public void setSelectedCaissierId(Long selectedCaissierId) {
        this.selectedCaissierId = selectedCaissierId;
    }
    
    public List<Utilisateur> getAvailableCaissiers() {
        return availableCaissiers;
    }
    
    public Long getSelectedProduitId() {
        return selectedProduitId;
    }
    
    public void setSelectedProduitId(Long selectedProduitId) {
        this.selectedProduitId = selectedProduitId;
    }
    
    public List<Produit> getAvailableProduits() {
        return availableProduits;
    }
}
