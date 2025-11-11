package com.example.retail.service;

import com.example.retail.domain.Commande;
import com.example.retail.domain.StatutCommande;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigDecimal;

@Stateless
public class CaisseService {
    @PersistenceContext
    EntityManager em;

    public void encaisser(Long commandeId, Double montant) {
        var c = em.find(Commande.class, commandeId);
        if (c == null) throw new IllegalArgumentException("Commande inconnue");
        c.setStatut(StatutCommande.PAYEE);
        c.setTotal(montant);
        em.merge(c);
        em.flush();
    }
}
