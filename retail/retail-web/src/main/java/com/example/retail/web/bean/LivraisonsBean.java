package com.example.retail.web.bean;

import com.example.retail.domain.Commande;
import com.example.retail.domain.Livraison;
import com.example.retail.domain.LivraisonEvent;
import com.example.retail.domain.StatutCommande;
import com.example.retail.service.CommandeService;
import com.example.retail.service.LivraisonService;
import com.example.retail.service.dto.PickItem;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class LivraisonsBean implements Serializable {
    @EJB
    private transient LivraisonService liv;
    @EJB
    private transient CommandeService commandes;
    private Long commandeId;
    private Long livraisonId;

    public java.util.List<Commande> getPayees() {
        return commandes.listByStatutBis(StatutCommande.PAYEE);
    }


    public void choisirCommande(Long id) {
        this.commandeId = id;
    }

    public java.util.List<PickItem> getItems() {
        return livraisonId == null ? java.util.Collections.emptyList() : liv.produitsALivrer(livraisonId);
    }

    public java.util.List<LivraisonEvent> getHistorique() {
        return livraisonId == null ? java.util.Collections.emptyList() : liv.historique(livraisonId);
    }

    public Livraison getLiv() {
        return livraisonId == null ? null : liv.get(livraisonId);
    }

    public void preparer() {
        try {
            livraisonId = liv.preparer(commandeId);
            msg("Livraison " + livraisonId + " préparée");
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    public void expedier() {
        try {
            System.out.println("experier success");
            liv.expedier(livraisonId);
            msg("Livraison " + livraisonId + " expédiée");
        } catch (Exception ex) {
            System.out.println("expedier erreru");
            err(ex.getMessage());
        }
    }

    public void livrer() {
        try {
            liv.livrer(livraisonId);
            msg("Livraison " + livraisonId + " livrée");
        } catch (Exception ex) {
            err(ex.getMessage());
        }
    }

    private void msg(String m) {
        var fc = javax.faces.context.FacesContext.getCurrentInstance();
        fc.addMessage(null, new javax.faces.application.FacesMessage(javax.faces.application.FacesMessage.SEVERITY_INFO, m, ""));
    }

    private void err(String m) {
        var fc = javax.faces.context.FacesContext.getCurrentInstance();
        fc.addMessage(null, new javax.faces.application.FacesMessage(javax.faces.application.FacesMessage.SEVERITY_ERROR, m, ""));
    }

    public java.util.List<Livraison> getDernieres() {
        return liv.dernieres(10);
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long v) {
        commandeId = v;
    }

    public Long getLivraisonId() {
        return livraisonId;
    }

    public void setLivraisonId(Long v) {
        livraisonId = v;
    }
}