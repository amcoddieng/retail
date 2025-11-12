package com.example.retail.web.bean;

import com.example.retail.domain.Catalogue;
import com.example.retail.domain.Famille;
import com.example.retail.service.AdminService;
import com.example.retail.service.CatalogueService;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Named
@SessionScoped
public class AdminFamilleBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(AdminFamilleBean.class.getName());

    @EJB
    private AdminService admin;
    
    @EJB
    private CatalogueService catalogueService;
    
    private Famille current = new Famille();
    private List<Long> catalogueIds = new ArrayList<>();
    private boolean showNew = false;
    private boolean editMode = false;

    public List<Famille> getFamilles() {
        return admin.familles();
    }

    public void nouveau() {
        current = new Famille();
        catalogueIds = new ArrayList<>();
        showNew = true;
        editMode = false;
        LOG.info("Mode création activé");
    }

    public void annuler() {
        showNew = false;
        editMode = false;
        current = new Famille();
        catalogueIds = new ArrayList<>();
        LOG.info("Création/Édition annulée");
    }

    public void edit(Long id) {
        try {
            current = admin.findFamilleById(id);
            if (current != null) {
                // Charger les IDs des catalogues associés
                catalogueIds = new ArrayList<>();
                for (Catalogue c : current.getCatalogues()) {
                    catalogueIds.add(c.getId());
                }
                editMode = true;
                LOG.info("Édition de la famille: " + current.getNom());
            } else {
                addErrorMessage("Famille non trouvée");
                LOG.warning("Famille non trouvée pour l'ID: " + id);
            }
        } catch (Exception e) {
            addErrorMessage("Erreur lors de l'édition: " + e.getMessage());
            LOG.severe("Erreur édition famille: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (current.getNom() == null || current.getNom().trim().isEmpty()) {
                addErrorMessage("Le nom de la famille est obligatoire");
                return;
            }
            
            // Sauvegarder d'abord la famille
            admin.saveFamille(current);
            
            // Mettre à jour les associations avec les catalogues
            if (current.getId() != null) {
                catalogueService.updateCataloguesForFamille(current.getId(), catalogueIds);
                
                // Recharger la famille avec les catalogues mis à jour
                current = admin.findFamilleById(current.getId());
            }
            
            // Réinitialiser après sauvegarde
            current = new Famille();
            catalogueIds = new ArrayList<>();
            showNew = false;
            editMode = false;
            
            addSuccessMessage("Famille enregistrée avec succès");
            LOG.info("Famille enregistrée avec " + catalogueIds.size() + " catalogues");
        } catch (Exception e) {
            String errorMsg = "Erreur lors de l'enregistrement de la famille: " + e.getMessage();
            LOG.severe(errorMsg);
            addErrorMessage(errorMsg);
        }
    }

    public void delete(Long id) {
        try {
            admin.deleteFamille(id);
            addSuccessMessage("Famille supprimée avec succès");
            LOG.info("Famille supprimée: " + id);
        } catch (com.example.retail.service.BusinessException ex) {
            addErrorMessage("Erreur suppression: " + ex.getMessage());
            LOG.warning("Erreur suppression famille: " + ex.getMessage());
        } catch (Exception e) {
            addErrorMessage("Erreur technique lors de la suppression");
            LOG.severe("Erreur technique suppression: " + e.getMessage());
        }
    }

    // Getters
    public Famille getCurrent() { return current; }
    public boolean isShowNew() { return showNew; }
    public boolean isEditMode() { return editMode; }

    // Méthodes utilitaires pour les messages
    // Méthodes pour la gestion des catalogues
    public List<Catalogue> getAllCatalogues() {
        return admin.catalogues();
    }
    
    public List<Catalogue> getSelectedCatalogues() {
        if (catalogueIds == null || catalogueIds.isEmpty()) {
            return new ArrayList<>();
        }
        return admin.catalogues().stream()
                .filter(c -> catalogueIds.contains(c.getId()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Getters et setters
    public List<Long> getCatalogueIds() {
        return catalogueIds;
    }

    public void setCatalogueIds(List<Long> catalogueIds) {
        this.catalogueIds = catalogueIds;
    }
    
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", message));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }

    public void editRemote() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String familyId = params.get("familyId");

        if (familyId != null && !familyId.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(familyId);
                edit(id);
                edit(id);
            } catch (NumberFormatException e) {
                addErrorMessage("ID invalide");
            }
        }
    }
}