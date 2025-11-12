package com.example.retail.web.bean;

import com.example.retail.domain.Role;
import com.example.retail.domain.Utilisateur;
import com.example.retail.service.AuthService;
import at.favre.lib.crypto.bcrypt.BCrypt;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

@Named("adminUsersBean")
@RequestScoped
public class AdminUsersBean implements Serializable {

    @EJB
    private AuthService auth;

    // Form de création
    private String login;
    private String name;
    private String email;
    private String password;
    private Set<String> roles = new LinkedHashSet<>();

    // Pour l'édition
    private Utilisateur selectedUser;
    private Set<String> selectedRoles = new LinkedHashSet<>();

    // Caches vue
    private List<Utilisateur> users;
    private List<Role> allRoles;
    
    private String telephone;
    private String codePostal;
    private String adresse;

    @PostConstruct
    public void init() {
        users = auth.listUsers();
        allRoles = auth.listRoles();
    }

   
    public void create() {
        try {
            String pwd = (password == null || password.isBlank()) ? "changeme" : password;

            // Hash côté admin si tu veux l’imposer ici (optionnel) :
            String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                             .hashToString(10, pwd.toCharArray());

            Long id = auth.registerClient(login, email, hash, name, telephone, codePostal, adresse);
            

            msgInfo("Utilisateur créé (id " + id + ").");
            users = auth.listUsers(); // refresh
            clearForm();
        } catch (Exception e) {
            msgWarn("Erreur création utilisateur : " + e.getMessage());
        }
    }



    private void clearForm() {
        login = name = email = password = telephone = codePostal = adresse = null;
       
        roles = null;
    }

    // --------- Pour le reset de mot de passe ----------
    public void resetPassword(Utilisateur u) {
        String hash = BCrypt.withDefaults().hashToString(12, "changeme".toCharArray());
        auth.setPasswordHash(u.getLogin(), hash);
        msgInfo("Mot de passe réinitialisé (changeme)");
    }

    // ... le reste de votre code reste identique ...
    public List<Utilisateur> getUsers() {
        if (users == null) {
            users = Optional.ofNullable(auth.listUsers()).orElseGet(Collections::emptyList);
        }
        return users;
    }

    public List<Role> getAllRoles() {
        if (allRoles == null) {
            allRoles = Optional.ofNullable(auth.listRoles()).orElseGet(Collections::emptyList);
        }
        return allRoles;
    }


    public void openEdit(Utilisateur u) {
        this.selectedUser = u;
        System.out.println(this.selectedUser);
        List<String> r = auth.rolesOf(u.getLogin());
        this.selectedRoles = new LinkedHashSet<>(Optional.ofNullable(r).orElseGet(Collections::emptyList));
    }

    public void saveRoles() {
        if (selectedUser == null) return;
        auth.setUserRoles(selectedUser.getLogin(), new ArrayList<>(selectedRoles));
        invalidateUsers();
        msgInfo("Rôles mis à jour");
    }

    public void setEnabled(Utilisateur u, boolean enabled) {
        auth.setEnabled(u.getLogin(), enabled);
        invalidateUsers();
        msgInfo("Statut mis à jour");
    }

    private void clearCreateForm() {
        login = email = password = null;
        roles.clear();
    }

    private void invalidateUsers() {
        users = null;
    }

    private static String str(String v) {
        return v == null ? "" : v.trim();
    }

    private static String nullIfBlank(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private void msgInfo(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, ""));
    }

    private void msgWarn(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, m, ""));
    }

    // Getters/Setters...
    public String getLogin() { return login; }
    public void setLogin(String v) { login = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> v) { roles = (v == null) ? new LinkedHashSet<>() : v; }
    public Utilisateur getSelectedUser() { return selectedUser; }
    public void setSelectedUser(Utilisateur u) { selectedUser = u; }
    public Set<String> getSelectedRoles() { return selectedRoles; }
    public void setSelectedRoles(Set<String> r) { selectedRoles = (r == null) ? new LinkedHashSet<>() : r; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsers(List<Utilisateur> users) {
        this.users = users;
    }


	public String getTelephone() {
		return telephone;
	}


	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}


	public String getCodePostal() {
		return codePostal;
	}


	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}


	public String getAdresse() {
		return adresse;
	}


	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
    
    
}