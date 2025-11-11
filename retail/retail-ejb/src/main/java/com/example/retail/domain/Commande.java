package com.example.retail.domain;

import javax.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Utilisateur client;
    @Enumerated(EnumType.STRING)
    private StatutCommande statut = StatutCommande.EN_ATTENTE_PAIEMENT;
    @Column(name = "created_at")
    private Date createdAt = new Date();
    private Double total = 0D;
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CommandeLigne> lignes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Utilisateur getClient() {
        return client;
    }

    public void setClient(Utilisateur u) {
        client = u;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande s) {
        statut = s;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double t) {
        total = t;
    }

    public java.util.List<CommandeLigne> getLignes() {
        return lignes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setLignes(List<CommandeLigne> lignes) {
        this.lignes = lignes;
    }
}