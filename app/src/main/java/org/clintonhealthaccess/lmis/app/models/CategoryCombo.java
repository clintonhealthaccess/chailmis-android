package org.clintonhealthaccess.lmis.app.models;

import java.util.List;

public class CategoryCombo {
    private String name;
    private String id;
    private List<CategoryOptionCombos> categoryOptionCombos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CategoryOptionCombos> getCategoryOptionCombos() {
        return categoryOptionCombos;
    }

    public void setCategoryOptionCombos(List<CategoryOptionCombos> categoryOptionCombos) {
        this.categoryOptionCombos = categoryOptionCombos;
    }
}
