package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class ExcelCategory {
    private String name;
    private List<ExcelCommodity> commodityList;

    public ExcelCategory(String categoryName) {
        name = categoryName;
        commodityList = new ArrayList<ExcelCommodity>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExcelCategory)) return false;

        ExcelCategory that = (ExcelCategory) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
