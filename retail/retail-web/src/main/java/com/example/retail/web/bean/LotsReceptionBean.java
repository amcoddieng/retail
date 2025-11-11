package com.example.retail.web.bean;

import com.example.retail.domain.LotAppro;
import javax.faces.view.ViewScoped;
import com.example.retail.service.FournisseurService;
import javax.annotation.PostConstruct;
import com.example.retail.service.StockService;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;


@Named("lotsReceptionBean")
@javax.faces.view.ViewScoped
public class LotsReceptionBean implements Serializable {

    @EJB
    private FournisseurService svc;
    
    @EJB
    private StockService stockService;

    private List<LotAppro> lots; 

    @PostConstruct
    public void init() {
        lots = stockService.listLotsAvecRelations();
    }

    public List<LotAppro> getLots() {
        return lots;
    }

   public String recu(Long id) {
        if (svc == null) {
            msgWarn("Service indisponible (dev) — aucune action réalisée.");
            return null;
        }
        try {
            svc.marquerRecu(id);
            msgInfo("Lot #" + id + " marqué REÇU.");
            refresh();
        } catch (Exception ex) {
            msgError("Impossible de marquer REÇU : " + ex.getMessage());
        }
        return null; // stay on page (Ajax)
    }

    public String conforme(Long id) {
        if (svc == null) {
            msgWarn("Service indisponible (dev) — aucune action réalisée.");
            return null;
       }
        try {
            svc.marquerConforme(id);
            msgInfo("Lot #" + id + " marqué CONFORME. Stock mis à jour.");
            refresh();
        } catch (Exception ex) {
            msgError("Impossible de marquer CONFORME : " + ex.getMessage());
        }
        return null;
    }

    private void refresh() {
        // force rechargement à l'affichage suivant
    	lots = stockService.listLotsAvecRelations();
    }

    /* -------- messages utilitaires -------- */
    private void msgInfo(String m)  { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,  m, "")); }
    private void msgWarn(String m)  { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,  m, "")); }
    private void msgError(String m) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, "")); }
}
