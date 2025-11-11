package com.example.retail.web.bean.model;

import com.example.retail.domain.Produit;
import com.example.retail.service.AdminService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class ProduitLazyModel extends LazyDataModel<Produit> {
    private final AdminService admin;
    private String filter;

    public ProduitLazyModel(AdminService admin) {
        this.admin = admin;
        setRowCount((int) admin.countProduits(null));
    }

    public void setFilter(String f) {
        this.filter = f;
        setRowCount((int) admin.countProduits(f));
    }


    @Override
    public List<Produit> load(int first,
                              int pageSize,
                              Map<String, SortMeta> sortBy,
                              Map<String, FilterMeta> filterBy) {
        String sortField = null;
        boolean asc = true;
        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta sm = sortBy.values().iterator().next();
            sortField = sm.getSortField();
            asc = sm.getSortOrder() == SortOrder.ASCENDING;
        }

        String f = filter;
        if (filterBy != null) {
            FilterMeta g = filterBy.get("global");
            if (g != null && g.getFilterValue() != null) {
                f = String.valueOf(g.getFilterValue());
            }
        }


        setRowCount((int) admin.countProduits(f));


        return admin.findProduitsPaged(first, pageSize, sortField, asc, f);
    }

    @Override
    public boolean isRowAvailable() {
        return false;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Produit getRowData() {
        return null;
    }

    @Override
    public int getRowIndex() {
        return 0;
    }



    @Override
    public String getRowKey(Produit produit) {
        return "";
    }

    @Override
    public Produit getRowData(String s) {
        return null;
    }
}