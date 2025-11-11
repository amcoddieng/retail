package com.example.retail.web.bean;

import com.example.retail.domain.Famille;
import com.example.retail.service.AdminService;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Named
@SessionScoped
public class AdminFamilleBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(AdminFamilleBean.class.getName());

    @EJB
    private AdminService admin;
    private Famille current = new Famille();
    private boolean showNew = false;
    private boolean editMode = false;

    public List<Famille> getFamilles() {
        return admin.familles();
    }

    public void nouveau() {
        current = new Famille();
        showNew = true;
        editMode = false;
        LOG.info("Mode création activé");
    }

    public void annuler() {
        showNew = false;
        editMode = false;
        current = new Famille();
        LOG.info("Création/Édition annulée");
    }

    public void edit(Long id) {
        try {
            current = admin.findFamilleById(id);
            if (current != null) {
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

            admin.saveFamille(current);

            // Réinitialiser après sauvegarde
            current = new Famille();
            showNew = false;
            editMode = false;

            addSuccessMessage("Famille enregistrée avec succès");
            LOG.info("Famille enregistrée");

        } catch (Exception e) {
            addErrorMessage("Erreur lors de l'enregistrement: " + e.getMessage());
            LOG.severe("Erreur enregistrement famille: " + e.getMessage());
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
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    public void editRemote() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String familyId = params.get("familyId");

        if (familyId != null && !familyId.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(familyId);
                edit(id);
            } catch (NumberFormatException e) {
                addErrorMessage("ID invalide");
            }
        }
    }
}