package com.example.retail.web.bean;

import com.example.retail.domain.Famille;
import com.example.retail.domain.Produit;
import com.example.retail.service.AdminService;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

@Named
@ViewScoped
public class AdminProduitBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(AdminProduitBean.class.getName());

    @EJB
    private AdminService admin;
    private Produit current = new Produit();
    private Long familleId;
    private boolean showNew = false;
    private boolean editMode = false;
    private List<Produit> filteredProduits;
    public AdminProduitBean() {

    }

    private UploadedFile uploaded;
    public void upload(FileUploadEvent evt) {
        try {
            var uf = evt.getFile();
            if (uf == null || uf.getContent() == null || uf.getSize() == 0) {
                addErrorMessage("Aucun fichier reçu.");
                return;
            }

            // 1) Taille
            long maxBytes = 10L * 1024 * 1024; // 10 MiB
            if (uf.getSize() > maxBytes) {
                addErrorMessage("Fichier trop volumineux (max 10 Mo).");
                return;
            }

            // 2) Type MIME (fiabilise 'image/jpg' -> 'image/jpeg')
            String ct = uf.getContentType();
            if ("image/jpg".equalsIgnoreCase(ct)) ct = "image/jpeg";

            // Autorisés
            java.util.Set<String> allowed = new java.util.HashSet<>();
            allowed.add("image/jpeg");
            allowed.add("image/png");
            allowed.add("image/gif");

            if (ct == null || !allowed.contains(ct.toLowerCase())) {
                addErrorMessage("Type non autorisé : " + ct + " (jpeg, png, gif uniquement).");
                return;
            }

  
            String name = uf.getFileName();
            String lower = name != null ? name.toLowerCase() : "";
            boolean hasGoodExt = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                              || lower.endsWith(".png") || lower.endsWith(".gif");
            if (!hasGoodExt) {
                addErrorMessage("Extension non autorisée.");
                return;
            }

            // 5) Enregistre dans ton entité
            current.setImage(uf.getContent());
            current.setImageContentType(ct);

            // 6) (Optionnel) Nettoie le nom d’affichage pour les logs/messages
            String safeName = sanitizeFileName(name);
            addSuccessMessage("Image téléchargée : " + safeName);
            LOG.info("Image uploadée: " + safeName + " (" + uf.getSize() + " bytes)");
        } catch (Exception e) {
            addErrorMessage("Erreur lors du téléchargement de l'image: " + e.getMessage());
            LOG.severe("Erreur upload image: " + e.getMessage());
        }
    }

    private String sanitizeFileName(String original) {
        if (original == null) return "image";
        String base = java.nio.file.Paths.get(original).getFileName().toString();
        // supprime diacritiques + remplace les caractères non sûrs par _
        String noAccent = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    public List<Produit> getProduits() {
        return admin.produits();
    }

    public List<Famille> getFamilles() {
        return admin.familles();
    }

    public void nouveau() {
        current = new Produit();
        familleId = null;
        showNew = true;
        editMode = false;
        LOG.info("Mode création produit activé");
    }

    public void annuler() {
        showNew = false;
        editMode = false;
        current = new Produit();
        familleId = null;
        LOG.info("Création/Édition produit annulée");
    }

    public void edit(Long id) {
        try {
            current = admin.findProduitById(id);
            if (current != null) {
                familleId = (current.getFamille() != null) ? current.getFamille().getId() : null;
                editMode = true;
                LOG.info("Édition du produit: " + current.getLibelle());
            } else {
                addErrorMessage("Produit non trouvé");
                LOG.warning("Produit non trouvé pour l'ID: " + id);
            }
        } catch (Exception e) {
            addErrorMessage("Erreur lors de l'édition: " + e.getMessage());
            LOG.severe("Erreur édition produit: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (current.getLibelle() == null || current.getLibelle().trim().isEmpty()) {
                addErrorMessage("Le libellé du produit est obligatoire");
                return;
            }

            if (current.getPrix() == null || current.getPrix() <= 0) {
                addErrorMessage("Le prix doit être supérieur à 0");
                return;
            }

            if (current.getStockDisponible() == null || current.getStockDisponible() < 0) {
                addErrorMessage("Le stock ne peut pas être négatif");
                return;
            }

            admin.saveProduit(current, familleId);

            // Réinitialiser après sauvegarde
            current = new Produit();
            familleId = null;
            showNew = false;
            editMode = false;

            addSuccessMessage("Produit enregistré avec succès");
            LOG.info("Produit enregistré");

        } catch (com.example.retail.service.BusinessException ex) {
            addErrorMessage("Erreur: " + ex.getMessage());
            LOG.warning("Erreur enregistrement produit: " + ex.getMessage());
        } catch (Exception e) {
            addErrorMessage("Erreur technique lors de l'enregistrement");
            LOG.severe("Erreur technique enregistrement produit: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        try {
            admin.deleteProduit(id);
            addSuccessMessage("Produit supprimé avec succès");
            LOG.info("Produit supprimé: " + id);
        } catch (com.example.retail.service.BusinessException ex) {
            addErrorMessage("Erreur suppression: " + ex.getMessage());
            LOG.warning("Erreur suppression produit: " + ex.getMessage());
        } catch (Exception e) {
            addErrorMessage("Erreur technique lors de la suppression");
            LOG.severe("Erreur technique suppression produit: " + e.getMessage());
        }
    }

    // Getters
    public Produit getCurrent() { return current; }
    public Long getFamilleId() { return familleId; }
    public void setFamilleId(Long familleId) { this.familleId = familleId; }
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

    public String getCurrentImageBase64() {
        if (current != null && current.getImage() != null && current.getImage().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(current.getImage());
            String contentType = current.getImageContentType() != null ?
                    current.getImageContentType() : "image/jpeg";
            return "data:" + contentType + ";base64," + base64;
        }
        return getDefaultImageBase64();
    }

    public String getProductImageBase64(Produit produit) {
        System.out.println("start  "+produit);
        System.out.println("start  "+produit.getId());
        if (produit.getImage() != null && produit.getImage().length > 0) {
            System.out.println("in  === "+produit.getImage());

            String base64 = Base64.getEncoder().encodeToString(produit.getImage());
            String contentType = produit.getImageContentType() != null ?
                    produit.getImageContentType() : "image/jpeg";
            return "data:" + contentType + ";base64," + base64;
        }
        return getDefaultImageBase64();
    }

    private String getDefaultImageBase64() {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";
    }

    public List<Produit> getFilteredProduits() { return filteredProduits; }
    public void setFilteredProduits(List<Produit> filteredProduits) {
        this.filteredProduits = filteredProduits;
    }
}