package com.example.retail.service;

import com.example.retail.domain.Catalogue;
import com.example.retail.domain.Famille;
import com.example.retail.domain.Produit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class CatalogueService {
    @PersistenceContext
    private EntityManager em;

    // Méthodes pour les catalogues
    public List<Catalogue> listerCatalogues() {
        return em.createQuery("SELECT c FROM Catalogue c ORDER BY c.nom", Catalogue.class).getResultList();
    }

    public Catalogue trouverCatalogueParId(Long id) {
        return em.find(Catalogue.class, id);
    }

    public Catalogue enregistrerCatalogue(Catalogue catalogue) {
        if (catalogue.getId() == null) {
            em.persist(catalogue);
            return catalogue;
        } else {
            return em.merge(catalogue);
        }
    }

    public void supprimerCatalogue(Long id) {
        Catalogue catalogue = trouverCatalogueParId(id);
        if (catalogue != null) {
            // Supprimer d'abord les associations avec les familles
            for (Famille famille : catalogue.getFamilles()) {
                famille.getCatalogues().remove(catalogue);
            }
            // Supprimer les associations avec les produits
            for (Produit produit : catalogue.getProduits()) {
                produit.getCatalogues().remove(catalogue);
            }
            em.remove(em.contains(catalogue) ? catalogue : em.merge(catalogue));
        }
    }

    // Méthodes pour les familles
    public List<Famille> listerFamilles() {
        return em.createQuery("SELECT f FROM Famille f ORDER BY f.nom", Famille.class).getResultList();
    }

    public List<Famille> listerFamillesParCatalogue(Long catalogueId) {
        return em.createQuery(
                "SELECT f FROM Famille f JOIN f.catalogues c WHERE c.id = :catalogueId ORDER BY f.nom", 
                Famille.class)
                .setParameter("catalogueId", catalogueId)
                .getResultList();
    }

    // Méthodes pour gérer les associations entre catalogues et familles
    public void ajouterFamilleAuCatalogue(Long catalogueId, Long familleId) {
        Catalogue catalogue = trouverCatalogueParId(catalogueId);
        Famille famille = em.find(Famille.class, familleId);
        
        if (catalogue != null && famille != null) {
            catalogue.addFamille(famille);
            em.merge(catalogue);
        }
    }

    public void retirerFamilleDuCatalogue(Long catalogueId, Long familleId) {
        Catalogue catalogue = trouverCatalogueParId(catalogueId);
        Famille famille = em.find(Famille.class, familleId);
        
        if (catalogue != null && famille != null) {
            catalogue.removeFamille(famille);
            em.merge(catalogue);
        }
    }
    
    /**
     * Met à jour les associations entre un catalogue et ses familles
     * @param catalogueId L'identifiant du catalogue
     * @param nouvellesFamilleIds Liste des identifiants des familles à associer
     */
    public void updateFamillesForCatalogue(Long catalogueId, List<Long> nouvellesFamilleIds) {
        Catalogue catalogue = trouverCatalogueParId(catalogueId);
        if (catalogue == null) {
            throw new IllegalArgumentException("Catalogue non trouvé avec l'ID: " + catalogueId);
        }
        
        // Liste des familles actuellement associées
        List<Famille> famillesActuelles = new ArrayList<>(catalogue.getFamilles());
        
        // Retirer les familles qui ne sont plus sélectionnées
        for (Famille famille : famillesActuelles) {
            if (!nouvellesFamilleIds.contains(famille.getId())) {
                catalogue.removeFamille(famille);
            }
        }
        
        // Ajouter les nouvelles familles sélectionnées
        for (Long familleId : nouvellesFamilleIds) {
            boolean existeDeja = false;
            for (Famille f : famillesActuelles) {
                if (f.getId().equals(familleId)) {
                    existeDeja = true;
                    break;
                }
            }
            if (!existeDeja) {
                Famille famille = em.find(Famille.class, familleId);
                if (famille != null) {
                    catalogue.addFamille(famille);
                }
            }
        }
        
        // Mettre à jour le catalogue
        em.merge(catalogue);
    }
    
    /**
     * Met à jour les associations entre une famille et ses catalogues
     * @param familleId L'identifiant de la famille
     * @param nouveauxCatalogueIds Liste des identifiants des catalogues à associer
     */
    public void updateCataloguesForFamille(Long familleId, List<Long> nouveauxCatalogueIds) {
        if (familleId == null) {
            throw new IllegalArgumentException("L'ID de la famille ne peut pas être nul");
        }
        
        // Charger la famille avec ses catalogues
        Famille famille = em.createQuery(
            "SELECT DISTINCT f FROM Famille f " +
            "LEFT JOIN FETCH f.catalogues " +
            "WHERE f.id = :id", Famille.class)
            .setParameter("id", familleId)
            .getSingleResult();
            
        if (famille == null) {
            throw new IllegalArgumentException("Famille non trouvée avec l'ID: " + familleId);
        }
        
        // Liste des catalogues actuellement associés
        List<Catalogue> cataloguesActuels = new ArrayList<>(famille.getCatalogues());
        
        // Retirer les catalogues qui ne sont plus sélectionnés
        for (Catalogue catalogue : cataloguesActuels) {
            if (!nouveauxCatalogueIds.contains(catalogue.getId())) {
                famille.removeCatalogue(catalogue);
            }
        }
        
        // Ajouter les nouveaux catalogues sélectionnés
        for (Long catalogueId : nouveauxCatalogueIds) {
            boolean existeDeja = false;
            for (Catalogue c : cataloguesActuels) {
                if (c.getId().equals(catalogueId)) {
                    existeDeja = true;
                    break;
                }
            }
            if (!existeDeja) {
                Catalogue catalogue = em.find(Catalogue.class, catalogueId);
                if (catalogue != null) {
                    famille.addCatalogue(catalogue);
                }
            }
        }
        
        // Mettre à jour la famille
        em.merge(famille);
    }
    
    /**
     * Récupère un catalogue avec ses familles chargées
     * @param id L'identifiant du catalogue
     * @return Le catalogue avec ses familles
     */
    public Catalogue trouverCatalogueAvecFamilles(Long id) {
        try {
            return em.createQuery(
                "SELECT DISTINCT c FROM Catalogue c " +
                "LEFT JOIN FETCH c.familles " +
                "WHERE c.id = :id", Catalogue.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
