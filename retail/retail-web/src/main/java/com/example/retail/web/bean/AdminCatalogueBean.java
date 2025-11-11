package com.example.retail.web.bean;

import com.example.retail.domain.Catalogue;
import com.example.retail.domain.Produit;
import com.example.retail.service.AdminService;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Named("adminCatalogueBean")
@SessionScoped
public class AdminCatalogueBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(AdminCatalogueBean.class.getName());

    @EJB
    private AdminService admin;
    private Catalogue current = new Catalogue();
    private List<Long> produitIds = new ArrayList<>();
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
        showNew = true;
        editMode = false;
        LOG.info("Mode création catalogue activé");
    }

    public void annuler() {
        showNew = false;
        editMode = false;
        current = new Catalogue();
        produitIds = new ArrayList<>();
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
            if (current.getNom() == null || current.getNom().trim().isEmpty()) {
                addErrorMessage("Le nom du catalogue est obligatoire");
                return;
            }

            System.out.println(produitIds);

            admin.saveCatalogue(current, produitIds);

            // Réinitialiser après sauvegarde
            current = new Catalogue();
            produitIds = new ArrayList<>();
            showNew = false;
            editMode = false;

            addSuccessMessage("Catalogue enregistré avec succès");
            LOG.info("Catalogue enregistré");

        } catch (com.example.retail.service.BusinessException ex) {
            addErrorMessage("Erreur: " + ex.getMessage());
            LOG.warning("Erreur enregistrement catalogue: " + ex.getMessage());
        } catch (Exception e) {
            addErrorMessage("Erreur technique lors de l'enregistrement");
            LOG.severe("Erreur technique enregistrement catalogue: " + e.getMessage());
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

    // Getters
    public Catalogue getCurrent() { return current; }
    public List<Long> getProduitIds() { return produitIds; }
    public void setProduitIds(List<Long> produitIds) { this.produitIds = produitIds; }
    public boolean isShowNew() { return showNew; }
    public boolean isEditMode() { return editMode; }

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