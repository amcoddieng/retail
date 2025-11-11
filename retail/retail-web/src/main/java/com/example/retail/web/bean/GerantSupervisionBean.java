package com.example.retail.web.bean;

import com.example.retail.domain.Utilisateur;
import com.example.retail.service.AuthService;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class GerantSupervisionBean {
    @EJB
    private AuthService auth;

    public java.util.List<Utilisateur> getUsers() {
        return auth.listUsers();
    }

    public void enable(String login) {
        auth.setEnabled(login, true);
        msg("Activé: " + login);
    }

    public void disable(String login) {
        auth.setEnabled(login, false);
        msg("Désactivé: " + login);
    }

    public void reset(String login) {
        auth.setPasswordHash(login, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("changeme"));
        msg("Réinitialisé: " + login);
    }

    private void msg(String m) {
        var fc = javax.faces.context.FacesContext.getCurrentInstance();
        fc.addMessage(null, new javax.faces.application.FacesMessage(javax.faces.application.FacesMessage.SEVERITY_INFO, m, ""));
    }
}