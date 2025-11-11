package com.example.retail.web.bean;

import com.example.retail.service.AuthService;
import com.example.retail.domain.Utilisateur;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.springframework.security.crypto.bcrypt.BCrypt;


import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {
    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());

    @EJB
    private AuthService authService;

    private String username;
    private String password;
    private String name;
    private boolean rememberMe;
    private Utilisateur loggedInUser;
    private List<String> userRoles;
    private MenuModel profileMenuModel;

    @PostConstruct
    public void init() {
        createProfileMenu();
    }

    private void createProfileMenu() {
        profileMenuModel = new DefaultMenuModel();

        // Sous-menu Profil
        DefaultSubMenu subMenu = DefaultSubMenu.builder()
                .label("Mon Profil")
                .build();

        // Élément Déconnexion
        DefaultMenuItem logoutItem = DefaultMenuItem.builder()
                .value("Déconnexion")
                .icon("pi pi-sign-out")
                .command("#{loginBean.logout}")
                .build();
        subMenu.getElements().add(logoutItem);

        profileMenuModel.getElements().add(subMenu);
    }

    public MenuModel getProfileMenuModel() {
        return profileMenuModel;
    }
    public String login() {
        try {
            // Validation des champs
            if (username == null || username.trim().isEmpty()) {
                addErrorMessage("Le nom d'utilisateur est requis");
                return null;
            }

            if (password == null || password.trim().isEmpty()) {
                addErrorMessage("Le mot de passe est requis");
                return null;
            }
            
            String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
            .hashToString(12, password.toCharArray());
            System.out.println("hash == "+hash);
            // Authentification manuelle
            Utilisateur user = authService.authenticate(username, hash);
            System.out.println(user);
            if (user == null) {
                addErrorMessage("Identifiants incorrects");
                return null;
            }
            System.out.println(user.getLogin());
            System.out.println(user.getPasswordHash());
            if(!BCrypt.checkpw(password,user.getPasswordHash())){
                System.out.println("Byc");
                addErrorMessage("Identifiants incorrects");
                return null;
            }

            if (!user.isActif()) {
                addErrorMessage("Compte désactivé. Contactez l'administrateur.");
                return null;
            }

            // Stocker l'utilisateur en session
            this.loggedInUser = user;
            this.name = user.getName();
            this.userRoles = authService.rolesOf(username);
            // Stocker dans la session HTTP
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            session.setAttribute("loggedInUser", user);
            session.setAttribute("username", username);
            session.setAttribute("userRoles", userRoles);

            LOG.info("Connexion réussie pour: " + username);
            addSuccessMessage("Connexion réussie !");

            // Redirection vers la page d'accueil
            return redirectByRole();

        } catch (Exception e) {
            LOG.severe("Erreur lors de la connexion: " + e.getMessage());
            addErrorMessage("Erreur technique lors de la connexion");
            return null;
        }
    }

    private String redirectByRole() {
        if (hasRole("ADMIN")) {
            return "/admin/utilisateurs.xhtml?faces-redirect=true";
        } else if (hasRole("GERANT")) {
            return "/gerant/supervision.xhtml?faces-redirect=true";
        } else if (hasRole("FOURNISSEUR")) {
            return "/fournisseur/lots.xhtml?faces-redirect=true";
        } else if (hasRole("LOGISTIQUE")) {
            return "/logistique/lots-reception.xhtml?faces-redirect=true";
        }
        else if (hasRole("CLIENT")) {
            return "/index.xhtml?faces-redirect=true";
        }
        else {
            return "/index.xhtml?faces-redirect=true";
        }
    }

    public String logout() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

            if (session != null) {
                session.invalidate();
            }

            this.loggedInUser = null;
            this.userRoles = null;
            this.username = null;
            this.password = null;
            this.name = null;

            LOG.info("Déconnexion réussie");

        } catch (Exception e) {
            LOG.severe("Erreur déconnexion: " + e.getMessage());
        }

        return "/index.xhtml?faces-redirect=true";
    }

    // Dans LoginBean
    public void checkAccess(String requiredRole) {
        if (!isUserLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("login.xhtml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requiredRole != null && !hasRole(requiredRole)) {
            try {
                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("access-denied.xhtml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasRole(String role) {
        return userRoles != null && userRoles.contains(role);
    }

    public boolean isUserLoggedIn() {
        return loggedInUser != null;
    }

    public String getCurrentUsername() {
        return loggedInUser != null ? loggedInUser.getLogin() : null;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }


    // Méthodes utilitaires pour les messages
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
        return;
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    // Getters et Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }

    public Utilisateur getLoggedInUser() { return loggedInUser; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoggedInUser(Utilisateur loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
 // ---------- Getters boolean pour JSF ----------
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isGerant() {
        return hasRole("GERANT");
    }

    public boolean isFournisseur() {
        return hasRole("FOURNISSEUR");
    }

    public boolean isLogistique() {
        return hasRole("LOGISTIQUE");
    }

    public boolean isCaissier() {
        return hasRole("CAISSIER");
    }

    public boolean isLivreur() {
        return hasRole("LIVREUR");
    }
    public boolean isGardien() {
        return hasRole("GARDIEN");
    }
    public boolean isGestionnaire() {
        return hasRole("GESTIONNAIRE");
    }
    public boolean isClient() {
        return hasRole("CLIENT");
    }

    public String redirectToLogin() {
        return "/login.xhtml?faces-redirect=true";
    }

}