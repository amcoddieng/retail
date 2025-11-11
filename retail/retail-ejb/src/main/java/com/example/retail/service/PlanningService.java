package com.example.retail.service;

import com.example.retail.domain.HoraireEmploye;
import com.example.retail.domain.Role;
import com.example.retail.domain.Utilisateur;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Stateless
public class PlanningService {

    @PersistenceContext
    EntityManager em;


    public List<HoraireEmploye> listAll() {
        return em.createQuery("select h from HoraireEmploye h left join fetch h.utilisateur order by h.utilisateur.login, h.jourSemaine, h.heureDebut", HoraireEmploye.class)
                 .getResultList();
    }

    public List<HoraireEmploye> listByUser(Long userId) {
        return em.createQuery("select h from HoraireEmploye h left join fetch h.utilisateur where h.utilisateur.id=:u order by h.jourSemaine, h.heureDebut", HoraireEmploye.class)
                 .setParameter("u", userId).getResultList();
    }


    public HoraireEmploye save(HoraireEmploye h, Long userId) {
        Utilisateur u = em.find(Utilisateur.class, userId);
        if (u == null) throw new IllegalArgumentException("Utilisateur introuvable");

        if (h.getHeureFin().isBefore(h.getHeureDebut()))
            throw new IllegalArgumentException("Heure fin avant heure début");

        h.setUtilisateur(u);
        if (h.getId() == null) {
            em.persist(h);
        } else {
            h = em.merge(h);
        }
        return h;
    }

 
    public void delete(Long id) {
        HoraireEmploye h = em.find(HoraireEmploye.class, id);
        if (h != null) em.remove(h);
    }

    // --- Utilitaire: savoir si un user est planifié à un instant ---
    public boolean isUserScheduledNow(String login) {
        LocalDate nowD = LocalDate.now();
        LocalTime nowT = LocalTime.now();
        int dow = java.time.DayOfWeek.from(nowD).getValue(); // 1=Lundi..7=Dimanche

        List<HoraireEmploye> list = em.createQuery(
                "select h from HoraireEmploye h where h.utilisateur.login=:l and h.jourSemaine=:d and h.actif=true " +
                "and (h.dateDebut is null or h.dateDebut<=:today) and (h.dateFin is null or h.dateFin>=:today) " +
                "and :now between h.heureDebut and h.heureFin", HoraireEmploye.class)
                .setParameter("l", login)
                .setParameter("d", dow)
                .setParameter("today", nowD)
                .setParameter("now", nowT)
                .getResultList();

        return !list.isEmpty();
    }
}
