package com.example.retail.service;

import com.example.retail.domain.*;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class FournisseurService {

    @PersistenceContext
    EntityManager em;

    public List<Commande> commandesDuFournisseur(String login) {
        return em.createQuery(
                        "select distinct c from Commande c " +
                                " join c.lignes l " +
                                " join l.produit p " +
                                " join l.fournisseur f " +
                                " where f.login = :l order by c.id desc", Commande.class)
                .setParameter("l", login)
                .getResultList();
    }

    public Long creerLot(String login) {
        Utilisateur f = em.createQuery(
                        "select f from Utilisateur f where f.login=:l", Utilisateur.class)
                .setParameter("l", login)
                .getSingleResult();

        LotAppro lot = new LotAppro();
        lot.setFournisseur(f);
        lot.setStatut(LotStatut.CREE);
        em.persist(lot);
        em.flush(); // Pour obtenir l'ID immédiatement
        return lot.getId();
    }

    public void ajouterItem(Long lotId, Long produitId, int qte) {
        LotAppro lot = em.find(LotAppro.class, lotId);
        Produit p = em.find(Produit.class, produitId);

        if (lot == null) throw new IllegalArgumentException("Lot inconnu");
        if (p == null)   throw new IllegalArgumentException("Produit inconnu");

        if (qte <= 0) {
            throw new IllegalArgumentException("Quantité invalide");
        }

        // Vérifier si l'item existe déjà
        LotItem existingItem = findExistingItem(lotId, produitId);
        if (existingItem != null) {
            // Mettre à jour la quantité si l'item existe déjà
            existingItem.setQuantite(existingItem.getQuantite() + qte);
        } else {
            // Créer un nouvel item
            LotItem it = new LotItem();
            it.setLot(lot);
            it.setProduit(p);
            it.setQuantite(qte);
            em.persist(it);

            // Initialiser la collection si nécessaire
            if (lot.getItems() == null) {
                // Cela dépend de comment votre entité est configurée
            }
        }
    }

    private LotItem findExistingItem(Long lotId, Long produitId) {
        try {
            return em.createQuery(
                    "SELECT i FROM LotItem i " +
                    "WHERE i.lot.id = :lotId AND i.produit.id = :produitId",
                    LotItem.class)
                .setParameter("lotId", lotId)
                .setParameter("produitId", produitId)
                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<LotAppro> lotsFournisseur(String login) {

        String jpql = "SELECT DISTINCT l FROM LotAppro l " +
                "LEFT JOIN FETCH l.items " +
                "WHERE l.fournisseur.login = :login " +
                "ORDER BY l.id DESC";
        return em.createQuery(
                jpql,
                LotAppro.class)
            .setParameter("login", login)
            .getResultList();
    }

    public List<LotAppro> lotsAControler() {
        return em.createQuery(
                "SELECT DISTINCT l FROM LotAppro l " +
                "LEFT JOIN FETCH l.fournisseur " +    
                "WHERE l.statut <> :s " +
                "ORDER BY l.id",
                LotAppro.class)
            .setParameter("s", LotStatut.CONFORME)
            .getResultList();
    }

    public void marquerRecu(Long lotId) {
        LotAppro l = em.find(LotAppro.class, lotId);
        if (l == null) throw new IllegalArgumentException("Lot inconnu");
        l.setStatut(LotStatut.RECU);
    }

    public void marquerConforme(Long lotId) {
        LotAppro l = em.find(LotAppro.class, lotId);
        if (l == null) throw new IllegalArgumentException("Lot inconnu");
        l.setStatut(LotStatut.CONFORME);

        if (l.getItems() != null) {
            for (LotItem it : l.getItems()) {
                Produit p = it.getProduit();
                if (p != null) {
                    Integer dispo = p.getStockDisponible() == null ? 0 : p.getStockDisponible();
                    p.setStockDisponible(dispo + it.getQuantite());
                }
            }
        }
    }

    public List<Utilisateur> listerFournisseurs() {
        return em.createQuery(
                        "SELECT DISTINCT ur.utilisateur FROM UtilisateurRole ur " +
                                "WHERE ur.role.code = 'FOURNISSEUR' " +
                                "AND ur.utilisateur.actif = true " +
                                "ORDER BY ur.utilisateur.login", Utilisateur.class)
                .getResultList();
    }
}