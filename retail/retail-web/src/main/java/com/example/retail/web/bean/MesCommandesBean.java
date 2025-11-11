package com.example.retail.web.bean;

import com.example.retail.domain.Commande;
import com.example.retail.service.AdminService;
import com.example.retail.service.CommandeService;

import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.context.SecurityContextHolder;

@Named
@RequestScoped
public class MesCommandesBean {
    @EJB
    private CommandeService commandes;

    @EJB
    private AdminService adminService;
    
    private Double totals;

    public java.util.List<Commande> getMesCommandes() {;
    	List<Commande> commande = commandes.listByLogin(getCurrentUserLogin());
    	totals = commande.stream().mapToDouble(cmd -> cmd.getTotal()).sum();
    	return commande;
    }

    private String getCurrentUserLogin() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                if (session != null) {
                    return (String) session.getAttribute("username");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur getCurrentUserLogin: " + e.getMessage());
        }
        return null;
    }

	public Double getTotals() {
		return totals;
	}

	public void setTotals(Double totals) {
		this.totals = totals;
	}
    
    

}