package com.example.retail.domain;

import javax.persistence.*;

import java.time.Instant;
import java.util.Date;

@Entity(name = "livraison_event")
public class LivraisonEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Livraison livraison;
    @Enumerated(EnumType.STRING)
    private LivraisonStatut statut;
    private Date timestamp = new Date();

    public Long getId() {
        return id;
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison l) {
        livraison = l;
    }

    public LivraisonStatut getStatut() {
        return statut;
    }

    public void setStatut(LivraisonStatut s) {
        statut = s;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date t) {
        timestamp = t;
    }
}