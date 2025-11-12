package com.example.retail.web.bean;

import com.example.retail.domain.Catalogue;
import com.example.retail.domain.Famille;
import com.example.retail.domain.Produit;
import com.example.retail.service.AdminService;
import com.example.retail.service.CatalogueService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Named("adminCatalogueBean")
@ViewScoped
public class AdminCatalogueBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(AdminCatalogueBean.class.getName());

    @EJB
    private AdminService admin;
    
    @EJB
    private CatalogueService catalogueService;
    
    private Catalogue current = new Catalogue();
    private List<Long> produitIds = new ArrayList<>();
    private List<Long> familleIds = new ArrayList<>();
    private boolean showNew = false;
    private boolean editMode = false;

    public List<Catalogue> getCatalogues() {
        return admin.catalogues();
    }

    public List<Produit> getProduits() {
        return admin.produits();
    }

    public void nouveau() {
        current = new Catalogue();
        produitIds = new ArrayList<>();
        familleIds = new ArrayList<>();
        showNew = true;
        editMode = false;
        LOG.info("Mode création catalogue activé");
    }

    public void annuler() {
        showNew = false;
        editMode = false;
        current = new Catalogue();
        produitIds = new ArrayList<>();
        familleIds = new ArrayList<>();
        LOG.info("Création/Édition catalogue annulée");
    }

    public void edit(Long id) {
        try {
            current = admin.findCatalogueById(id);
            if (current != null) {
                produitIds = new ArrayList<>();
                for (Produit p : current.getProduits()) {
                    produitIds.add(p.getId());
                }
                
                familleIds = new ArrayList<>();
                for (Famille f : current.getFamilles()) {
                    familleIds.add(f.getId());
                }
                
                editMode = true;
                LOG.info("Édition du catalogue: " + current.getNom());
            } else {
                addErrorMessage("Catalogue non trouvé");
                LOG.warning("Catalogue non trouvé pour l'ID: " + id);
            }
        } catch (Exception e) {
            addErrorMessage("Erreur lors de l'édition: " + e.getMessage());
            LOG.severe("Erreur édition catalogue: " + e.getMessage());
        }
    }

    public void save() {
        try {
            // Validation des entrées
            if (current.getNom() == null || current.getNom().trim().isEmpty()) {
                addErrorMessage("Le nom du catalogue est obligatoire");
                return;
            }
            
            if (familleIds == null || familleIds.isEmpty()) {
                addErrorMessage("Veuillez sélectionner au moins une famille");
                return;
            }

            boolean isNew = current.getId() == null;
            
            try {
                // Sauvegarder d'abord le catalogue
                current = admin.saveCatalogue(current, produitIds);
                
                // Mettre à jour les associations avec les familles
                if (current.getId() != null) {
                    catalogueService.updateFamillesForCatalogue(current.getId(), familleIds);
                    
                    // Recharger le catalogue avec les familles mises à jour
                    current = admin.findCatalogueById(current.getId());
                    
                    // Si c'est une création, ajouter le nouveau catalogue à la liste
                    if (isNew) {
                        // La liste des catalogues sera rechargée automatiquement par getCatalogues()
                        LOG.info("Nouveau catalogue créé avec ID: " + current.getId());
                    }
                    
                    // Réinitialiser après sauvegarde réussie
                    resetForm();
                    addSuccessMessage("Catalogue enregistré avec succès");
                    LOG.info("Catalogue enregistré avec succès. ID: " + current.getId());
                } else {
                    throw new Exception("Erreur lors de la sauvegarde: l'ID du catalogue est null");
                }
                
            } catch (javax.persistence.PersistenceException pe) {
                String errorMsg = "Erreur de persistance lors de la sauvegarde du catalogue";
                LOG.log(Level.SEVERE, errorMsg, pe);
                addErrorMessage(errorMsg + ": " + pe.getMessage());
            } catch (com.example.retail.service.BusinessException be) {
                LOG.warning("Erreur métier lors de l'enregistrement: " + be.getMessage());
                addErrorMessage(be.getMessage());
            } catch (Exception e) {
                String errorMsg = "Erreur inattendue lors de l'enregistrement du catalogue";
                LOG.log(Level.SEVERE, errorMsg, e);
                addErrorMessage(errorMsg + ": " + e.getMessage());
            }
            
        } catch (Exception e) {
            String errorMsg = "Erreur critique lors de l'enregistrement";
            LOG.log(Level.SEVERE, errorMsg, e);
            addErrorMessage(errorMsg + ": " + e.getMessage());
        }
    }

    public void delete(Long id) {
        try {
            admin.deleteCatalogue(id);
            addSuccessMessage("Catalogue supprimé avec succès");
            LOG.info("Catalogue supprimé: " + id);
        } catch (com.example.retail.service.BusinessException ex) {
            addErrorMessage("Erreur suppression: " + ex.getMessage());
            LOG.warning("Erreur suppression catalogue: " + ex.getMessage());
        } catch (Exception e) {
            addErrorMessage("Erreur technique lors de la suppression");
            LOG.severe("Erreur technique suppression catalogue: " + e.getMessage());
        }
    }

    // Méthodes pour la gestion des familles
    public List<Famille> getAllFamilles() {
        return admin.familles();
    }
    
    public List<Famille> getSelectedFamilles() {
        if (familleIds == null || familleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return admin.familles().stream()
                .filter(f -> familleIds.contains(f.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Méthode pour réinitialiser le formulaire
    private void resetForm() {
        this.current = new Catalogue();
        this.produitIds = new ArrayList<>();
        this.familleIds = new ArrayList<>();
        this.showNew = false;
        this.editMode = false;
    }

    // Getters et setters
    public Catalogue getCurrent() {
        return current;
    }

    public void setCurrent(Catalogue current) {
        this.current = current;
    }

    public List<Long> getProduitIds() { 
        return produitIds; 
    }

    public void setProduitIds(List<Long> produitIds) { 
        this.produitIds = produitIds; 
    }

    public List<Long> getFamilleIds() { 
        return familleIds; 
    }

    public void setFamilleIds(List<Long> familleIds) { 
        this.familleIds = familleIds; 
    }

    public List<Famille> getFamilles() { 
        return current.getFamilles(); 
    }

    public boolean isShowNew() { 
        return showNew; 
    }

    public boolean isEditMode() { 
        return editMode; 
    }

    // Méthodes utilitaires pour les messages
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
}