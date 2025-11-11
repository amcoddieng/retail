package com.example.retail.web.bean;

import com.example.retail.domain.Produit;
import com.example.retail.service.AdminService;
import org.primefaces.model.StreamedContent;
import org.springframework.web.context.annotation.SessionScope;
import org.primefaces.model.DefaultStreamedContent;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Map;

@Named("imageBean")
@SessionScope // Important pour que ça fonctionne
public class ImageBean implements Serializable {

    @EJB
    private AdminService admin;

    public StreamedContent getImage() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return getDefaultImage();
        }
        System.out.println("context === "+context);
        // Récupérer l'ID depuis les paramètres de la vue
        String productId = context.getExternalContext().getRequestParameterMap().get("productId");

        System.out.println("productId avant === "+productId);
        if (productId == null || productId.isEmpty()) {
            return getDefaultImage();
        }
        System.out.println("productId apres === "+productId);
        try {
            Long id = Long.valueOf(productId);
            Produit p = admin.findProduitById(id);

            if (p != null && p.getImage() != null && p.getImage().length > 0) {
                return DefaultStreamedContent.builder()
                        .contentType(p.getImageContentType() != null ? p.getImageContentType() : "image/jpeg")
                        .stream(() -> new ByteArrayInputStream(p.getImage()))
                        .build();
            }
        } catch (NumberFormatException e) {
            // Ignorer et retourner l'image par défaut
        }

        return getDefaultImage();
    }
    
    public StreamedContent productImage(Long productId) {
        if (productId == null) {
            return getDefaultImage();
        }

        try {
            Produit p = admin.findProduitById(productId);
            if (p != null && p.getImage() != null && p.getImage().length > 0) {
                final byte[] bytes = p.getImage(); // capture dans la closure
                final String contentType = (p.getImageContentType() != null && !p.getImageContentType().isEmpty())
                        ? p.getImageContentType()
                        : "image/jpeg";

                return DefaultStreamedContent.builder()
                        .contentType(contentType)
                        .name("prod-" + productId) // facultatif
                        .stream(() -> new ByteArrayInputStream(bytes))
                        .build();
            }
        } catch (Exception e) {
            // (optionnel) logger proprement
            // e.g. Logger.getLogger(ImageBean.class.getName()).log(Level.WARNING, "Erreur image produit " + productId, e);
        }

        return getDefaultImage();
    }


    private StreamedContent getDefaultImage() {
        byte[] px = new byte[]{(byte)137,80,78,71,13,10,26,10,0,0,0,13,73,72,68,82,0,0,0,1,0,0,0,1,8,6,0,0,0,31,-21,120,-46,0,0,0,12,73,68,65,84,120,-38,99,96,0,0,0,2,0,1,-38,33,-9,120,0,0,0,0,73,69,78,68,-82,66,96,-126};
        return DefaultStreamedContent.builder()
                .contentType("image/png")
                .stream(() -> new ByteArrayInputStream(px))
                .build();
    }
}