package com.example.retail.web.bean.model;

import com.example.retail.domain.Catalogue;
import com.example.retail.service.AdminService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class CatalogueLazyModel extends LazyDataModel<Catalogue> {
    private final AdminService admin;
    private String filter;

    public CatalogueLazyModel(AdminService admin) {
        this.admin = admin;
        setRowCount((int) admin.countCatalogues(null));
    }

    public void setFilter(String f) {
        this.filter = f;
        setRowCount((int) admin.countCatalogues(f));
    }


    @Override
    public List<Catalogue> load(int first,
                                int pageSize,
                                Map<String, SortMeta> sortBy,
                                Map<String, FilterMeta> filterBy) {
        // 1) Tri
        String sortField = null;
        boolean asc = true; // défaut
        if (sortBy != null && !sortBy.isEmpty()) {

            SortMeta sm = sortBy.values().iterator().next();
            sortField = sm.getSortField();
            asc = sm.getSortOrder() == SortOrder.ASCENDING;
        }

        String f = null;
        if (filterBy != null) {
            FilterMeta g = filterBy.get("global");
            if (g != null && g.getFilterValue() != null) {
                f = String.valueOf(g.getFilterValue());
            }
        }

        setRowCount((int) admin.countCatalogues(f));

        return admin.findCataloguesPaged(first, pageSize, sortField, asc, f);
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
    public Catalogue getRowData() {
        return null;
    }

    @Override
    public int getRowIndex() {
        return 0;
    }

    @Override
    public String getRowKey(Catalogue catalogue) {
        return "";
    }

    @Override
    public Catalogue getRowData(String s) {
        return null;
    }
}