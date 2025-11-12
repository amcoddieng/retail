package com.example.retail.service;

import com.example.retail.domain.Produit;
import com.example.retail.domain.LotAppro;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class StockService {

    @PersistenceContext
    EntityManager em;
 
    /** Pour la table et le selectOneMenu */

    public List<Produit> listProduits() {
        return em.createQuery("select p from Produit p order by p.id", Produit.class)
                .getResultList();
    }

    /** Ancien format Map (si tu veux le garder) */
    public Map<Long, Integer> tableau() {
        Map<Long, Integer> map = new LinkedHashMap<>();
        for (Produit p : listProduits()) {
            Integer dispo = p.getStockDisponible() == null ? 0 : p.getStockDisponible();
            map.put(p.getId(), dispo);
        }
        return map;
    }

    public void ajouter(Long produitId, int quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("Quantité invalide");
        Produit p = em.find(Produit.class, produitId);
        if (p == null) throw new IllegalArgumentException("Produit inconnu");
        Integer dispo = p.getStockDisponible() == null ? 0 : p.getStockDisponible();
        p.setStockDisponible(dispo + quantite);
        // entité managée → flush automatique en fin de tx
    }

    public void fournir(Map<Long, Integer> lots) {
        if (lots == null || lots.isEmpty()) return;
        for (Map.Entry<Long, Integer> e : lots.entrySet()) {
            ajouter(e.getKey(), e.getValue());
        }
    }
    public List<LotAppro> listLotsAvecRelations() {
        try {
            return em.createQuery(
                "SELECT DISTINCT l FROM LotAppro l " +
                "LEFT JOIN FETCH l.fournisseur " +
                "LEFT JOIN FETCH l.items " +
                "ORDER BY l.id DESC",
                LotAppro.class
            ).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur listLotsAvecRelations : " + e.getMessage());
            return List.of();
        }
    }

}
