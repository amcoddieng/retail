package com.example.retail.service;

import com.example.retail.domain.Boutique;
import com.example.retail.domain.Produit;
import com.example.retail.domain.Utilisateur;
import com.example.retail.service.BusinessException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;

@Stateless
public class BoutiqueService {

    @PersistenceContext
    private EntityManager em;
    
    public Boutique creerBoutique(Boutique boutique, Utilisateur gerant) {
        if (boutique == null || gerant == null) {
            throw new BusinessException("La boutique et le gérant sont obligatoires");
        }
        
        // S'assurer que l'utilisateur est géré par le contexte de persistance
        Utilisateur managedGerant = em.merge(gerant);
        
        // Définir le gérant de la boutique
        boutique.setGerant(managedGerant);
        
        // Initialiser la collection si nécessaire
        if (managedGerant.getBoutiquesGerees() == null) {
            managedGerant.setBoutiquesGerees(new HashSet<>());
        }
        
        // Ajouter la boutique à la liste des boutiques gérées par l'utilisateur
        managedGerant.getBoutiquesGerees().add(boutique);
        
        // Sauvegarder la boutique
        em.persist(boutique);
        
        return boutique;
    }
    
    public Boutique trouverParId(Long id) {
        return em.find(Boutique.class, id);
    }
    
    public List<Boutique> listerToutesLesBoutiques() {
        TypedQuery<Boutique> query = em.createQuery("SELECT b FROM Boutique b WHERE b.active = true", Boutique.class);
        return query.getResultList();
    }
    
    public List<Boutique> listerBoutiquesParGerant(Utilisateur gerant) {
        TypedQuery<Boutique> query = em.createQuery(
            "SELECT b FROM Boutique b WHERE b.gerant = :gerant AND b.active = true", 
            Boutique.class
        );
        query.setParameter("gerant", gerant);
        return query.getResultList();
    }
    
    public Boutique mettreAJourBoutique(Boutique boutique) {
        if (boutique == null || boutique.getId() == null) {
            throw new BusinessException("Boutique invalide");
        }
        return em.merge(boutique);
    }
    
    public void supprimerBoutique(Long id) {
        Boutique boutique = trouverParId(id);
        if (boutique != null) {
            // Désactiver plutôt que supprimer physiquement
            boutique.setActive(false);
            em.merge(boutique);
        }
    }
    
    public void ajouterCaissierABoutique(Long boutiqueId, Utilisateur caissier) {
        if (!caissier.hasRole("CAISSIER")) {
            throw new BusinessException("L'utilisateur n'est pas un caissier");
        }
        
        Boutique boutique = trouverParId(boutiqueId);
        if (boutique == null) {
            throw new BusinessException("Boutique introuvable");
        }
        
        boutique.addCaissier(caissier);
        caissier.getBoutiquesEnTantQueCaissier().add(boutique);
        
        em.merge(boutique);
        em.merge(caissier);
    }
    
    public void retirerCaissierDeBoutique(Long boutiqueId, Utilisateur caissier) {
        if (boutiqueId == null || caissier == null) {
            throw new BusinessException("La boutique et le caissier sont obligatoires");
        }
        
        Boutique boutique = trouverParId(boutiqueId);
        if (boutique == null) {
            throw new BusinessException("Boutique introuvable");
        }
        
        // Retirer le caissier de la boutique
        boutique.removeCaissier(caissier);
        caissier.getBoutiquesEnTantQueCaissier().remove(boutique);
        
        em.merge(boutique);
        em.merge(caissier);
    }
    
    public void ajouterProduitABoutique(Long boutiqueId, Produit produit) {
        if (boutiqueId == null || produit == null) {
            throw new BusinessException("La boutique et le produit sont obligatoires");
        }
        
        Boutique boutique = trouverParId(boutiqueId);
        if (boutique == null) {
            throw new BusinessException("Boutique introuvable");
        }
        
        // Vérifier si le produit n'est pas déjà dans la boutique
        if (boutique.getProduits().contains(produit)) {
            throw new BusinessException("Ce produit est déjà dans la boutique");
        }
        
        // Ajouter le produit à la boutique
        boutique.getProduits().add(produit);
        produit.getBoutiques().add(boutique);
        
        em.merge(boutique);
        em.merge(produit);
    }
    
    public void retirerProduitDeBoutique(Long boutiqueId, Produit produit) {
        if (boutiqueId == null || produit == null) {
            throw new BusinessException("La boutique et le produit sont obligatoires");
        }
         
        Boutique boutique = trouverParId(boutiqueId);
        if (boutique == null) {
            throw new BusinessException("Boutique introuvable");
        }
        
        // Retirer le produit de la boutique
        boutique.getProduits().remove(produit);
        produit.getBoutiques().remove(boutique);
        
        em.merge(boutique);
        em.merge(produit);
        em.merge(boutique);
    }
    
    public void retirerProduitDeBoutique(Long boutiqueId, Long produitId) {
        Boutique boutique = trouverParId(boutiqueId);
        if (boutique == null) {
            throw new BusinessException("Boutique introuvable");
        }
        
        Produit produit = em.find(Produit.class, produitId);
        if (produit == null) {
            throw new BusinessException("Produit introuvable");
        }
        
        boutique.removeProduit(produit);
        em.merge(boutique);
    }
    
    public List<Produit> listerProduitsParBoutique(Long boutiqueId) {
        TypedQuery<Produit> query = em.createQuery(
            "SELECT p FROM Produit p JOIN p.boutiques b WHERE b.id = :boutiqueId", 
            Produit.class
        );
        query.setParameter("boutiqueId", boutiqueId);
        return query.getResultList();
    }
    
    public List<Utilisateur> listerCaissiersParBoutique(Long boutiqueId) {
        TypedQuery<Utilisateur> query = em.createQuery(
            "SELECT u FROM Utilisateur u JOIN u.boutiquesEnTantQueCaissier b WHERE b.id = :boutiqueId", 
            Utilisateur.class
        );
        query.setParameter("boutiqueId", boutiqueId);
        return query.getResultList();
    }
}
