package com.example.retail.domain;

import javax.persistence.*;

@Entity
@Table(name = "lot_item")
public class LotItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private LotAppro lot;
    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    private Integer quantite;

    public Long getId() {
        return id;
    }

    public LotAppro getLot() {
        return lot;
    }

    public void setLot(LotAppro l) {
        lot = l;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit p) {
        produit = p;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer q) {
        quantite = q;
    }

    @Override
    public String toString() {
        return "LotItem{" +
                "id=" + id +
                ", lot=" + lot +
                ", produit=" + produit +
                ", quantite=" + quantite +
                '}';
    }
}