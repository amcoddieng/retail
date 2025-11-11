package com.example.retail.domain;

import javax.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "lot_appro")
public class LotAppro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Utilisateur fournisseur;
    @Column(name = "created_at")
    private Date createdAt = new Date();
    @Enumerated(EnumType.STRING)
    private LotStatut statut = LotStatut.CREE;
    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Utilisateur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Utilisateur f) {
        fournisseur = f;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public LotStatut getStatut() {
        return statut;
    }

    public void setStatut(LotStatut s) {
        statut = s;
    }

    public List<LotItem> getItems() {
        return items;
    }

    public void setItems(List<LotItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "LotAppro{" +
                "id=" + id +
                ", fournisseur=" + fournisseur +
                ", createdAt=" + createdAt +
                ", statut=" + statut +
                '}';
    }
}