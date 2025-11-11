package com.example.retail.web.bean;

import com.example.retail.service.AuthService;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.primefaces.event.FileUploadEvent;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;

@Named("registrationBean")
@RequestScoped
public class RegistrationBean implements Serializable {

    @EJB
    private AuthService auth;

    private String login;
    private String email;
    private String password;
    private String confirm;

    // NOUVEAUX CHAMPS
    private String name;
    private String telephone;
    private String codePostal;
    private String adresse;


    public String register() {
        try {
            if (password == null || !password.equals(confirm)) {
                addErr("Les mots de passe ne correspondent pas");
                return null;
            }
            String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            auth.registerClient(login, email, hash, name, telephone, codePostal, adresse);
            addInfo("Compte créé. Vous pouvez vous connecter.");
            return "/login.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            addErr(ex.getMessage());
            return null;
        } catch (Exception ex) {
            addErr("Erreur technique : " + ex.getMessage());
            return null;
        }
    }

    private void addInfo(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }
    private void addErr(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // Getters/setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirm() { return confirm; }
    public void setConfirm(String confirm) { this.confirm = confirm; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

}
