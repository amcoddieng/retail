package com.example.retail.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String login;
    private String name;
    private String email;
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;
    private boolean actif = true;
    private String telephone;

    @Column(length = 16, name = "code_postal")
    private String codePostal;

    @Column(length = 512)
    private String adresse;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "utilisateur_role",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "gerant")
    private Set<Boutique> boutiquesGerees = new HashSet<>();
    
    @ManyToMany(mappedBy = "caissiers")
    private Set<Boutique> boutiquesEnTantQueCaissier = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String s) {
        login = s;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String s) {
        email = s;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String s) {
        passwordHash = s;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean a) {
        actif = a;
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
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    public boolean hasRole(String roleCode) {
        return roles.stream().anyMatch(r -> r.getCode().equals(roleCode));
    }
    
    public Set<Boutique> getBoutiquesGerees() {
        return boutiquesGerees;
    }
    
    public void setBoutiquesGerees(Set<Boutique> boutiquesGerees) {
        this.boutiquesGerees = boutiquesGerees;
    }
    
    public Set<Boutique> getBoutiquesEnTantQueCaissier() {
        return boutiquesEnTantQueCaissier;
    }
    
    public void setBoutiquesEnTantQueCaissier(Set<Boutique> boutiquesEnTantQueCaissier) {
        this.boutiquesEnTantQueCaissier = boutiquesEnTantQueCaissier;
    }
}