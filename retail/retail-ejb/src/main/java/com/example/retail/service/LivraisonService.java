package com.example.retail.service;

import com.example.retail.domain.*;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class LivraisonService {
    @PersistenceContext
    EntityManager em;


    public Long preparer(Long commandeId) {
        var c = em.find(Commande.class, commandeId);
        if (c == null) throw new IllegalArgumentException("Commande inconnue");
        if (c.getStatut() != StatutCommande.PAYEE) throw new IllegalStateException("Commande non payée");
        var liv = new Livraison();
        liv.setCommande(c);
        em.persist(liv);
        {
            var ev = new LivraisonEvent();
            ev.setLivraison(liv);
            ev.setStatut(LivraisonStatut.PREPAREE);
            em.persist(ev);
        }
        var lignes = em.createQuery("select cl from commande_ligne cl where cl.commande.id=:id", CommandeLigne.class).setParameter("id", commandeId).getResultList();
        for (var cl : lignes) {
            var ll = new LivraisonLigne();
            ll.setLivraison(liv);
            ll.setProduit(cl.getProduit());
            ll.setQuantite(cl.getQuantite());
            em.persist(ll);
            liv.getLignes().add(ll);
        }
        return liv.getId();
    }


    public void expedier(Long livraisonId) {
        var l = em.find(Livraison.class, livraisonId);
        if (l == null) throw new IllegalArgumentException("Livraison inconnue");
        l.setStatut(LivraisonStatut.EXPEDIEE);
        {
            var ev = new LivraisonEvent();
            ev.setLivraison(l);
            ev.setStatut(LivraisonStatut.EXPEDIEE);
            em.persist(ev);
        }
    }

    public void livrer(Long livraisonId) {
        var l = em.find(Livraison.class, livraisonId);
        if (l == null) throw new IllegalArgumentException("Livraison inconnue");
        l.setStatut(LivraisonStatut.LIVREE);
        {
            var ev = new LivraisonEvent();
            ev.setLivraison(l);
            ev.setStatut(LivraisonStatut.LIVREE);
            em.persist(ev);
        }
    }

    public java.util.List<com.example.retail.service.dto.PickItem> produitsALivrer(Long livraisonId) {
        var list = new java.util.ArrayList<com.example.retail.service.dto.PickItem>();
        var lls = em.createQuery("select ll from livraison_ligne ll where ll.livraison.id=:id", LivraisonLigne.class).setParameter("id", livraisonId).getResultList();
        for (var ll : lls)
            list.add(new com.example.retail.service.dto.PickItem(ll.getProduit().getId(), ll.getProduit().getLibelle(), ll.getQuantite()));
        return list;
    }

    public java.util.List<LivraisonEvent> historique(Long livraisonId) {
        return em.createQuery("select e from livraison_event e where e.livraison.id=:id order by e.timestamp", LivraisonEvent.class).setParameter("id", livraisonId).getResultList();
    }

    public Livraison get(Long id) {
        return em.find(Livraison.class, id);
    }

    public java.util.List<Livraison> dernieres(int max) {
        return em.createQuery("select l from Livraison l order by l.id desc", Livraison.class).setMaxResults(max).getResultList();
    }

    public double calculerDelaiMoyenLivraisonV2() {
        try {
            List<Double> resultats = em.createQuery(
                            "SELECT AVG(TIMESTAMPDIFF(HOUR, l.createdAt, MAX(ev.timestamp))) " +
                                    "FROM Livraison l " +
                                    "JOIN l.events ev " +
                                    "WHERE l.statut = :statutTermine " +
                                    "GROUP BY l.id", Double.class)
                    .setParameter("statutTermine", LivraisonStatut.LIVREE)
                    .getResultList();

            return resultats != null && !resultats.isEmpty() && resultats.get(0) != null ?
                    Math.round(resultats.get(0) * 10.0) / 10.0 : 0.0;
        } catch (Exception e) {
            System.err.println("Erreur calcul délai moyen livraison V2: " + e.getMessage());
            return 0.0;
        }
    }

    public int compterRupturesStock() {
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(p) FROM Produit p WHERE p.stockDisponible <= 0", Long.class)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            System.err.println("Erreur comptage ruptures stock: " + e.getMessage());
            return 0;
        }
    }


}