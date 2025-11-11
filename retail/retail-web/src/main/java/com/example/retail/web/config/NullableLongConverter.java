package com.example.retail.web.config;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("nullableLong")
public class NullableLongConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext ctx, UIComponent cmp, String value) {
        if (value == null || value.trim().isEmpty()) return null; // "" -> null
        try { return Long.valueOf(value); } catch (NumberFormatException e) { return null; }
    }
    @Override
    public String getAsString(FacesContext ctx, UIComponent cmp, Object value) {
        return (value == null) ? "" : String.valueOf(value);
    }
}
