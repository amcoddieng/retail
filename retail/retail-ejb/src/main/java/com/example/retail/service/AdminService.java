package com.example.retail.service;

import com.example.retail.domain.Catalogue;
import com.example.retail.domain.Famille;
import com.example.retail.domain.Produit;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class AdminService {
    @PersistenceContext
    EntityManager em;

    // --- Produits ---
    public List<Produit> produits() {
        return em.createQuery("select p from Produit p order by p.id desc", Produit.class).getResultList();
    }

    public Produit saveProduit(Produit p, Long familleId) {
        if (familleId != null) {
            p.setFamille(em.find(Famille.class, familleId));
        }
        if (p.getId() == null) em.persist(p);
        else p = em.merge(p);
        return p;
    }

    public Produit findProduitById(Long id) {
        return em.find(Produit.class, id);
    }

    public void deleteProduit(Long id) {
        Produit p = em.find(Produit.class, id);
        if (p == null) return;
        Long usagesCmd = em.createQuery("select count(cl) from commande_ligne cl where cl.produit.id=:id", Long.class)
                .setParameter("id", id).getSingleResult();
        Long usagesCat = em.createQuery("select count(c) from Catalogue c join c.produits cp where cp.id=:id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (usagesCmd > 0 || usagesCat > 0)
            throw new BusinessException("Impossible de supprimer : produit référencé dans " + usagesCmd + " ligne(s) de commande et " + usagesCat + " catalogue(s).");
        em.remove(p);
    }

    public List<Famille> familles() {
        return em.createQuery("select f from Famille f order by f.nom", Famille.class).getResultList();
    }

    public Famille findFamilleById(Long id) {
        return em.find(Famille.class, id);
    }

    public Famille saveFamille(Famille f) {
        if (f.getId() == null) em.persist(f);
        else f = em.merge(f);
        return f;
    }

    public void deleteFamille(Long id) {
        Famille f = em.find(Famille.class, id);
        if (f == null) return;
        Long usages = em.createQuery("select count(p) from Produit p where p.famille.id=:id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (usages > 0)
            throw new BusinessException("Famille utilisée par " + usages + " produit(s). Réassignez les produits avant suppression.");
        em.remove(f);
    }

    // --- Catalogues ---
    public List<Catalogue> catalogues() {
        return em.createQuery("select c from Catalogue c left join fetch c.produits order by c.id desc", Catalogue.class).getResultList();
    }

    public Catalogue findCatalogueById(Long id) {
        return em.createQuery("select c from Catalogue c left join fetch c.produits where c.id = :id order by c.id desc",Catalogue.class).setParameter("id", id).getSingleResult();
    }

    public Catalogue saveCatalogue(Catalogue c, List<Long> produitIds) {
        List<Produit> list = produitIds == null ? java.util.Collections.emptyList() :
                em.createQuery("select p from Produit p where p.id in :ids", Produit.class).setParameter("ids", produitIds).getResultList();
        c.setProduits(list);
        System.out.println(list);
        if (c.getId() == null) em.persist(c);
        else c = em.merge(c);
        return c;
    }

    public void deleteCatalogue(Long id) {
        Catalogue c = em.find(Catalogue.class, id);
        if (c != null) em.remove(c);
    }

    // --- Pagination Produits --- CORRIGÉ
    public java.util.List<Produit> findProduitsPaged(int first, int pageSize, String sortField, boolean asc, String filter) {
        String sf = (sortField == null || sortField.isBlank()) ? "p.id" : "p." + sortField;
        String dir = asc ? "asc" : "desc";

        String jpql = "select p from Produit p ";

        if (filter != null && !filter.isBlank()) {
            jpql += "where lower(p.libelle) like lower(concat('%', :f, '%')) ";
        }

        jpql += "order by " + sf + " " + dir;

        TypedQuery<Produit> query = em.createQuery(jpql, Produit.class);

        if (filter != null && !filter.isBlank()) {
            query.setParameter("f", filter);
        }

        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public long countProduits(String filter) {
        String jpql = "select count(p) from Produit p ";

        if (filter != null && !filter.isBlank()) {
            jpql += "where lower(p.libelle) like lower(concat('%', :f, '%'))";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);

        if (filter != null && !filter.isBlank()) {
            query.setParameter("f", filter);
        }

        return query.getSingleResult();
    }

    // --- Pagination Catalogues --- CORRIGÉ
    public java.util.List<Catalogue> findCataloguesPaged(int first, int pageSize, String sortField, boolean asc, String filter) {
        String sf = (sortField == null || sortField.isBlank()) ? "c.id" : "c." + sortField;
        String dir = asc ? "asc" : "desc";

        String jpql = "select c from Catalogue c ";

        if (filter != null && !filter.isBlank()) {
            jpql += "where lower(c.nom) like lower(concat('%', :f, '%')) ";
        }

        jpql += "order by " + sf + " " + dir;

        TypedQuery<Catalogue> query = em.createQuery(jpql, Catalogue.class);

        if (filter != null && !filter.isBlank()) {
            query.setParameter("f", filter);
        }

        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public long countCatalogues(String filter) {
        String jpql = "select count(c) from Catalogue c ";

        if (filter != null && !filter.isBlank()) {
            jpql += "where lower(c.nom) like lower(concat('%', :f, '%'))";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);

        if (filter != null && !filter.isBlank()) {
            query.setParameter("f", filter);
        }

        return query.getSingleResult();
    }
}