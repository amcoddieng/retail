package com.example.retail.service;

import com.example.retail.domain.*;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
public class CommandeService {
    @PersistenceContext
    EntityManager em;

    public java.util.List<Produit> listerProduits() {
        var ps = em.createQuery("select p from Produit p", Produit.class).getResultList();
        if (ps.isEmpty()) {
            for (int i = 1; i <= 6; i++) {
                var p = new Produit();
                p.setLibelle("Produit " + i);
                p.setPrix(10D * i);
                p.setStockDisponible(3 + i);
                em.persist(p);
            }
            ps = em.createQuery("select p from Produit p", Produit.class).getResultList();
        }
        return ps;
    }

    public Commande passerCommande(Long clientId, Map<Long, Integer> lignes) {
        Utilisateur client = em.find(Utilisateur.class, clientId);
        if (client == null) {
            client = new Utilisateur();
            client.setLogin("client" + clientId);
            em.persist(client);
        }
        Commande c = new Commande();
        c.setClient(client);
        em.persist(c);
        Double total = 0D;
        for (var e : lignes.entrySet()) {
            Produit p = em.find(Produit.class, e.getKey());
            if (p == null) continue;
            int q = e.getValue();
            if (p.getStockDisponible() < q) throw new IllegalStateException("Stock insuffisant");
            p.setStockDisponible(p.getStockDisponible() - q);
            CommandeLigne cl = new CommandeLigne();
            cl.setCommande(c);
            cl.setProduit(p);
            cl.setQuantite(q);
            cl.setPrixUnitaire(p.getPrix());
            em.persist(cl);
            c.getLignes().add(cl);
            total = total + (p.getPrix() * q);
        }
        c.setTotal(total);
        return c;
    }

    public void encaisser(Long commandeId, java.math.BigDecimal montant) {
        var c = em.find(Commande.class, commandeId);
        if (c != null) c.setStatut(StatutCommande.PAYEE);
    }

    public java.util.List<Commande> listByStatut(StatutCommande statut) {
        return em.createQuery("select c from Commande c where c.statut=:s order by c.id desc", Commande.class).setParameter("s", statut).getResultList();
    }


    public java.util.List<Commande> listByStatutBis(StatutCommande statut) {
        return em.createQuery(
                        "SELECT c FROM Commande c " +
                                "WHERE c.statut = :s " +
                                "AND c.id NOT IN (" +
                                "    SELECT l.commande.id FROM Livraison l" +
                                ") " +
                                "ORDER BY c.id DESC",
                        Commande.class)
                .setParameter("s", statut)
                .getResultList();
    }

    public java.util.List<Commande> listByLogin(String login) {
        return em.createQuery("select c from Commande c where c.client.login=:l order by c.id desc", Commande.class)
                .setParameter("l", login).getResultList();
    }

    public Commande passerCommande(String login, Map<Long, Integer> lignes) {
        Utilisateur client = em.createQuery("select u from Utilisateur u where u.login=:l", Utilisateur.class)
                .setParameter("l", login).getResultStream().findFirst().orElse(null);
        Commande c = new Commande();
        c.setClient(client);
        em.persist(c);
        Double total = 0D;
        for (var e : lignes.entrySet()) {
            Produit p = em.find(Produit.class, e.getKey());
            if (p == null) continue;
            int q = e.getValue();
            if (p.getStockDisponible() < q) throw new IllegalStateException("Stock insuffisant");
            p.setStockDisponible(p.getStockDisponible() - q);
            em.persist(p);
            CommandeLigne cl = new CommandeLigne();
            cl.setCommande(c);
            cl.setProduit(p);
            cl.setQuantite(q);
            cl.setPrixUnitaire(p.getPrix());
            em.persist(cl);
            c.getLignes().add(cl);
            total = total + (p.getPrix() * q);
        }
        c.setTotal(total);
        return c;
    }

    public Commande findDetail(Long id) {
        System.out.println("Recherche commande détail: " + id);
        try {
            var list = em.createQuery(
                            "SELECT DISTINCT c FROM Commande c " +
                                    "LEFT JOIN FETCH c.lignes " +  // Pas d'alias après FETCH
                                    "LEFT JOIN FETCH c.client " +  // Pas d'alias après FETCH
                                    "WHERE c.id = :id",
                            Commande.class)
                    .setParameter("id", id)
                    .getResultList();

            Commande commande = list.isEmpty() ? null : list.get(0);
            System.out.println("Commande trouvée: " + (commande != null));
            return commande;

        } catch (Exception e) {
            System.err.println("Erreur recherche commande " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public int compterVentesDuJour() {
        try {
            Calendar aujourdhui = Calendar.getInstance();
            aujourdhui.set(Calendar.HOUR_OF_DAY, 0);
            aujourdhui.set(Calendar.MINUTE, 0);
            aujourdhui.set(Calendar.SECOND, 0);
            aujourdhui.set(Calendar.MILLISECOND, 0);

            Date debutJour = aujourdhui.getTime();

            Long count = em.createQuery(
                            "SELECT COUNT(c) FROM Commande c WHERE c.createdAt >= :debutJour", Long.class)
                    .setParameter("debutJour", debutJour)
                    .getSingleResult();

            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            System.err.println("Erreur comptage ventes du jour: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Récupère les données de ventes des 7 derniers jours
     */
    public List<Object[]> getVentes7DerniersJours() {
        try {
            return em.createQuery(
                            "SELECT FUNCTION('DATE', c.createdAt), COUNT(c) " +
                                    "FROM Commande c " +
                                    "WHERE c.createdAt >= FUNCTION('DATE', CURRENT_DATE - 7) " +
                                    "GROUP BY FUNCTION('DATE', c.createdAt) " +
                                    "ORDER BY FUNCTION('DATE', c.createdAt)", Object[].class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Erreur récupération ventes 7 jours: " + e.getMessage());
            return List.of();
        }
    }
    public EntityManager getEntityManager() {
        return em;
    }
//ce que j'ai ajoute
    // --- AJOUT POUR LA PAGE CAISSE ---

    public List<Commande> listerCommandesEnAttente() {
        try {
            List<Commande> commandes = em.createQuery(
                    "SELECT DISTINCT c FROM Commande c " +
                    "LEFT JOIN FETCH c.client " +
                    "WHERE c.statut = :statut " +
                    "ORDER BY c.id DESC", Commande.class)
                    .setParameter("statut", StatutCommande.EN_ATTENTE_PAIEMENT)
                    .getResultList();

            // Forcer le chargement complet avant fermeture du contexte
            for (Commande c : commandes) {
                if (c.getClient() != null) {
                    c.getClient().getLogin(); // touche le client pour le "réveiller"
                }
                if (c.getLignes() != null) {
                    c.getLignes().size(); // force le chargement des lignes si Lazy
                }
            }
            return commandes;
        } catch (Exception e) {
            System.err.println("Erreur listerCommandesEnAttente : " + e.getMessage());
            return List.of();
        }
    }


    public Commande trouverAvecLignes(Long id) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT c FROM Commande c " +
                    "LEFT JOIN FETCH c.client " +
                    "LEFT JOIN FETCH c.lignes " +
                    "WHERE c.id = :id", Commande.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            System.err.println("Erreur trouverAvecLignes : " + e.getMessage());
            return null;
        }
    }

}
