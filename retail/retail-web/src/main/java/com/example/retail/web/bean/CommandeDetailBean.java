package com.example.retail.web.bean;

import com.example.retail.domain.Commande;
import com.example.retail.domain.CommandeLigne;
import com.example.retail.service.CommandeService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;


@Named
@RequestScoped
public class CommandeDetailBean {
    @EJB
    private CommandeService service;

    private Long id;
    private Commande commande;

    public Long getId() {
        if (id == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                Map<String, String> params = context.getExternalContext().getRequestParameterMap();
                String idParam = params.get("id");
                if (idParam != null && !idParam.isEmpty()) {
                    try {
                        id = Long.valueOf(idParam);
                    } catch (NumberFormatException e) {
                        System.err.println("ID invalide: " + idParam);
                    }
                }
            }
        }
        return id;
    }
    public Commande getCommande() {
        Long commandeId = getId();
        if (commandeId == null) {
            return null;
        }
        return service.trouverAvecLignes(commandeId);
    }
}

//    public Commande getCommande() {
//        if (commande == null) {
//            Long commandeId = getId();
//            if (commandeId != null) {
//                // Utilisation de la méthode complète
//                commande = service.trouverAvecLignes(commandeId);
//                if (commande == null) {
//                    System.out.println("Commande #" + commandeId + " introuvable.");
//                }
//            }
//        }
//        return commande;
//    }
//}

//public class CommandeDetailBean {
//    @EJB
//    private CommandeService service;
//    private Long id;
//
//    // Récupérer l'ID depuis les paramètres de requête
//    public Long getId() {
//        if (id == null) {
//            FacesContext context = FacesContext.getCurrentInstance();
//            if (context != null) {
//                Map<String, String> params = context.getExternalContext().getRequestParameterMap();
//                String idParam = params.get("id");
//                if (idParam != null && !idParam.isEmpty()) {
//                    try {
//                        id = Long.valueOf(idParam);
//                    } catch (NumberFormatException e) {
//                        System.err.println("ID invalide: " + idParam);
//                    }
//                }
//            }
//        }
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Commande getCommande() {
//        Long commandeId = getId();
//
//        if (commandeId == null) {
//            return null;
//        }
//        System.out.println("commandeId="+ commandeId);
//        return service.findDetail(commandeId);
//    }
//}