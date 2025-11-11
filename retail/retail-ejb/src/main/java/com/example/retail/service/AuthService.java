package com.example.retail.service;

import com.example.retail.domain.*;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.security.Principal;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class AuthService {
    @PersistenceContext
    EntityManager em;

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    public Utilisateur findByLogin(String login) {
        var list = em.createQuery("select u from Utilisateur u where u.login=:l", Utilisateur.class).setParameter("l", login).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> rolesOf(String login) {
        return em.createQuery("select r.role.code from UtilisateurRole r where r.utilisateur.login=:l", String.class)
                .setParameter("l", login).getResultList();
    }

    // bootstrap: create user with roles if missing
    public void ensureUser(String login, String email, String passwordHash, String name,String... roles) {
        Utilisateur u = findByLogin(login);
        if (u == null) {
            u = new Utilisateur();
            u.setLogin(login);
            u.setEmail(email);
            u.setName(name);
            u.setPasswordHash(passwordHash);
            u.setActif(true);
            em.persist(u);
        }
        for (String rc : roles) {
            Role r = em.createQuery("select r from Role r where r.code=:c", Role.class).setParameter("c", rc).getResultStream().findFirst().orElse(null);
            if (r == null) {
                r = new Role();
                r.setCode(rc);
                em.persist(r);
            }
            Long count = em.createQuery("select count(ur) from UtilisateurRole ur where ur.utilisateur.id=:u and ur.role.id=:r", Long.class)
                    .setParameter("u", u.getId()).setParameter("r", r.getId()).getSingleResult();
            if (count == 0) {
                UtilisateurRole ur = new UtilisateurRole();
                ur.setUtilisateur(u);
                ur.setRole(r);
                em.persist(ur);
            }
        }
    }

    public void setPasswordHash(String login, String bcryptHash) {
        Utilisateur u = findByLogin(login);
        if (u == null) throw new IllegalArgumentException("Inconnu");
        u.setPasswordHash(bcryptHash);
    }

    public void setEnabled(String login, boolean enabled) {
        Utilisateur u = findByLogin(login);
        if (u != null) u.setActif(enabled);
    }


    public String createResetToken(String login, long seconds) {
        Utilisateur u = findByLogin(login);
        if (u == null) throw new IllegalArgumentException("Inconnu");
        PasswordResetToken t = new PasswordResetToken();
        t.setUtilisateur(u);
        String tok = java.util.UUID.randomUUID().toString();
        t.setToken(tok);
        t.setExpiresAt(java.time.Instant.now().plusSeconds(seconds));
        em.persist(t);
        return tok;
    }


    public java.util.List<Utilisateur> listUsers() {
        return em.createQuery("select u from Utilisateur u order by u.login", Utilisateur.class).getResultList();
    }
    
    public java.util.List<Utilisateur> listUsersEmployes() {
        return em.createQuery("select u.utilisateur from UtilisateurRole u where u.role.code NOT IN ('CLIENT', 'FOURNISSEUR') order by u.utilisateur.login", Utilisateur.class).getResultList();
    }

    public java.util.List<Role> listRoles() {
        List<Role> roles = em.createQuery("select r from Role r order by r.code", Role.class).getResultList();
        System.out.println(roles);
        return roles;
    }

    public void setUserRoles(String login, java.util.List<String> roles) {
        Utilisateur u = findByLogin(login);
        if (u == null) throw new IllegalArgumentException("Inconnu");
        em.createQuery("delete from UtilisateurRole ur where ur.utilisateur.id=:id").setParameter("id", u.getId()).executeUpdate();
        for (String code : roles) {
            Role r = em.createQuery("select r from Role r where r.code=:c", Role.class).setParameter("c", code).getResultStream().findFirst().orElse(null);
            if (r == null) {
                r = new Role();
                r.setCode(code);
                em.persist(r);
            }
            UtilisateurRole ur = new UtilisateurRole();
            ur.setUtilisateur(u);
            ur.setRole(r);
            em.persist(ur);
        }
    }

    public boolean isUserActive(String username) {
        try {
            TypedQuery<Utilisateur> query = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.login = :username", Utilisateur.class);
            query.setParameter("username", username);

            Utilisateur user = query.getSingleResult();
            return user != null && user.isActif();
        } catch (Exception e) {
            System.out.println("Utilisateur non trouvé ou erreur: " + username);
            return false;
        }
    }

    public Utilisateur authenticate(String username, String password) {
        try {
            System.out.println(username);
            TypedQuery<Utilisateur> query = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.login = :username", Utilisateur.class);
            query.setParameter("username", username);
            List<Utilisateur> results = query.getResultList();
            System.out.println(results.size());
            if (results.isEmpty()) {
                return null;
            }

            Utilisateur user = results.get(0);

            // Vérifier le mot de passe (supposant que passwordHash contient le hash BCrypt)
            if (user.getPasswordHash() != null) {
                return user;
            }

            return null;

        } catch (Exception e) {
            LOG.warning("Erreur d'authentification pour: " + username + " - " + e.getMessage());
            return null;
        }

    }
    
    public boolean loginExists(String login) {
        Long n = em.createQuery("select count(u) from Utilisateur u where lower(u.login)=:l", Long.class)
                   .setParameter("l", login == null ? "" : login.toLowerCase())
                   .getSingleResult();
        return n != 0;
    }

    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) return false;
        Long n = em.createQuery("select count(u) from Utilisateur u where lower(u.email)=:e", Long.class)
                   .setParameter("e", email.toLowerCase())
                   .getSingleResult();
        return n != 0;
    }

    public void ensureRoleClient() {
        Long n = em.createQuery("select count(r) from Role r where r.code='CLIENT'", Long.class).getSingleResult();
        if (n == 0) {
            Role r = new Role();
            r.setCode("CLIENT");
            em.persist(r);
        }
    }

    /** Nouvelle version complète, reçoit TOUT et stocke le hash. */
    public Long registerClient(String login,
                               String email,
                               String passwordHash,
                               String name,
                               String telephone,
                               String codePostal,
                               String adresse) {
        if (login == null || login.isBlank())
            throw new IllegalArgumentException("Login obligatoire");
        if (passwordHash == null || passwordHash.isBlank())
            throw new IllegalArgumentException("Mot de passe (hash) manquant");
        if (loginExists(login))
            throw new IllegalStateException("Ce login est déjà utilisé");
        if (email != null && !email.isBlank() && emailExists(email))
            throw new IllegalStateException("Cet email est déjà utilisé");

        ensureRoleClient();
        Role client = em.createQuery("select r from Role r where r.code='CLIENT'", Role.class)
                        .getSingleResult();

        Utilisateur u = new Utilisateur();
        u.setLogin(login);
        u.setEmail(email);
        u.setName(name);
        u.setTelephone(telephone);
        u.setCodePostal(codePostal);
        u.setAdresse(adresse);
        u.setPasswordHash(passwordHash);
        u.setActif(true);

        em.persist(u);                               // persist AVANT de créer la relation rôle
        UtilisateurRole ur = new UtilisateurRole();
        ur.setUtilisateur(u);
        ur.setRole(client);
        em.persist(ur);

        return u.getId();
    }

    /** Ancienne signature conservée pour compatibilité. */
    public Long registerClient(String login, String email, String passwordHash) {
        return registerClient(login, email, passwordHash, null, null, null, null);
    }

}
