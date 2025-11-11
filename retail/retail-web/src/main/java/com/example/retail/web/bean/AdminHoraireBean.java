package com.example.retail.web.bean;

import com.example.retail.domain.HoraireEmploye;
import com.example.retail.domain.Utilisateur;
import com.example.retail.service.AuthService;
import com.example.retail.service.PlanningService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Named
@RequestScoped
public class AdminHoraireBean implements Serializable {

    @EJB
    private PlanningService planning;

    @EJB
    private AuthService auth; // tu as déjà un service auth pour lister les users/roles

    private Long selectedUserId;
    private HoraireEmploye current = new HoraireEmploye();
    private boolean showNew = false;
    private boolean editMode = false;

    // Données
    public List<Utilisateur> getEmployesPlanifiables() {
        // on filtre côté xhtml si besoin; sinon ajoute une méthode côté AuthService qui retourne
        // uniquement CAISSIER / GESTIONNAIRE / GERANT
        return auth.listUsersEmployes();
    }

    public List<HoraireEmploye> getHoraires() {
        return (selectedUserId == null) ? planning.listAll()
                                        : planning.listByUser(selectedUserId);
    }

    public void onUserChange() {
        System.out.println("=== onUserChange called ===");
        System.out.println("Selected user ID: " + selectedUserId);
        
        showNew = false;
        editMode = false;
        current = new HoraireEmploye();
        
        // Forcer le rafraîchissement
        getHoraires(); // Cette méthode sera rappelée par le dataTable
        
        System.out.println("Horaires count: " + getHoraires().size());
    }


    public void nouveau() {
        current = new HoraireEmploye();
        current.setActif(true);
        current.setHeureDebut(LocalTime.of(8, 0));
        current.setHeureFin(LocalTime.of(17, 0));
        showNew = true;
        editMode = false;
    }

    public void edit(Long id) {
        for (HoraireEmploye h : getHoraires()) {
            if (h.getId().equals(id)) {
                current = h;
                selectedUserId = h.getUtilisateur().getId();
                showNew = true;
                editMode = true;
                break;
            }
        }
    }

    public void cancel() {
        showNew = false;
        editMode = false;
        current = new HoraireEmploye();
    }

    public void save() {
        planning.save(current, selectedUserId);
        cancel();
        addInfo("Horaire enregistré");
    }

    public void delete(Long id) {
        planning.delete(id);
        addInfo("Horaire supprimé");
    }
    
    public void search() {
        // On ferme le formulaire pour éviter les incohérences pendant le filtrage
        showNew = false;
        editMode = false;
        current = new HoraireEmploye();
    }

    public void clearFilter() {
        selectedUserId = null; // "Tous"
        search();              // même reset UI
    }

    // Helpers JSF
    private void addInfo(String m) {
        var fc = javax.faces.context.FacesContext.getCurrentInstance();
        fc.addMessage(null, new javax.faces.application.FacesMessage(javax.faces.application.FacesMessage.SEVERITY_INFO, m, ""));
    }

    // Getters/Setters
    public Long getSelectedUserId() { return selectedUserId; }
    public void setSelectedUserId(Long selectedUserId) { this.selectedUserId = selectedUserId; }
    public HoraireEmploye getCurrent() { return current; }
    public boolean isShowNew() { return showNew; }
    public boolean isEditMode() { return editMode; }
}
