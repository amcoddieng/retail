package com.example.retail.web.bean;

import com.example.retail.domain.Commande;
import com.example.retail.domain.StatutCommande;
import com.example.retail.service.CaisseService;
import com.example.retail.service.CommandeService;
import javax.faces.view.ViewScoped;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Named("caisseBean")
@ViewScoped
public class CaisseBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dateToday = new Date().toString();

    public CaisseBean() { } // requis pour passivation

    @EJB
    private transient CaisseService caisse;

    @EJB
    private transient CommandeService commandes;

    private Long commandeId;
    private Double montant;

    private transient Commande commandeSelectionnee;

//    public List<Commande> getEnAttente() {
//        return commandes.listByStatut(StatutCommande.EN_ATTENTE_PAIEMENT);
//    }
    //ce que j'ai fait 
    public List<Commande> getEnAttente() {
        return commandes.listerCommandesEnAttente();
    }


    public void choisir(Long id) {
        this.commandeId = id;
        rechargerSelection();
    }

    public void encaisser() {
        System.out.println("=== TENTATIVE ENCAISSEMENT ===");
        System.out.println("Commande ID: " + commandeId);
        System.out.println("Montant reçu: " + montant);

        try {

            System.out.println("=== TENTATIVE ENCAISSEMENT ===");
            System.out.println("Commande ID: " + commandeId);
            System.out.println("Montant reçu: " + montant);
            System.out.println("Montant reçu: " + montant);
            if(montant != null && montant > 0 && commandeSelectionnee.getTotal() > montant) {
            	var fc = javax.faces.context.FacesContext.getCurrentInstance();
                fc.addMessage(null, new javax.faces.application.FacesMessage(
                        javax.faces.application.FacesMessage.SEVERITY_ERROR,
                        "Erreur: Veuillez saisir un montant supérieur au total" , ""
                ));
                return;
            }
            caisse.encaisser(commandeId, commandeSelectionnee.getTotal());

            var fc = javax.faces.context.FacesContext.getCurrentInstance();
            fc.addMessage(null, new javax.faces.application.FacesMessage(
                    javax.faces.application.FacesMessage.SEVERITY_INFO,
                    "Paiement OK pour commande #" + commandeId, ""
            ));

            // réinitialise l'état de la vue
            this.commandeId = null;
            this.montant = null;
            this.commandeSelectionnee = null;

        } catch (Exception ex) {
            System.out.println("=== TENTATIVE ENCAISSEMENT ERREUR ===");
            System.out.println("Commande ID: " + commandeId);
            System.out.println("Montant reçu: " + montant);
            var fc = javax.faces.context.FacesContext.getCurrentInstance();
            fc.addMessage(null, new javax.faces.application.FacesMessage(
                    javax.faces.application.FacesMessage.SEVERITY_ERROR,
                    "Erreur: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()), ""
            ));
        }
    }

    public Commande getCommandeSelectionnee() {
        if (commandeId == null) return null;
        if (commandeSelectionnee == null) {
            rechargerSelection();
        }
        return commandeSelectionnee;
    }

    private void rechargerSelection() {
        if (commandeId == null) {
            commandeSelectionnee = null;
            return;
        }
        List<Commande> liste = getEnAttente();
        if (liste == null) {
            commandeSelectionnee = null;
            return;
        }
        commandeSelectionnee = liste.stream()
                .filter(c -> Objects.equals(c.getId(), commandeId))
                .findFirst()
                .orElse(null);
    }

    public Double getRenduMonnaie() {
        if (montant == null || montant <= 0 || commandeId == null) return 0.0;
        Commande cmd = getCommandeSelectionnee();
        if (cmd == null || cmd.getTotal() == null) return 0.0;
        return montant - cmd.getTotal();
    }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long v) { this.commandeId = v; }

    public Double getMontant() { return montant; }
    public void setMontant(Double v) { this.montant = v; }

    public Double getTotalEnAttente() {
        List<Commande> list = getEnAttente();
        if (list == null || list.isEmpty()) return 0.0;
        double total = 0.0;
        for (Commande c : list) {
            if (c.getTotal() != null) total += c.getTotal();
        }
        return total;
    }

    public String getDateToday() {
        return dateToday;
    }

    public void setDateToday(String dateToday) {
        this.dateToday = dateToday;
    }
}
