package org.clintonhealthaccess.lmis.app.models;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Category {
    private String name;
    private List<Commodity> commodities;

    public Category(String name, String... commodityNames) {
        this.name = name;
        commodities = Commodity.buildList(commodityNames);
    }

    public static List<Category> all() {
        Category firstCategory = new Category("Anti Malarials", "Coartem", "Choloquine", "Quinine", "Fansida", "Hedex", "Septrin");
        return ImmutableList.of(
                firstCategory, new Category("Antenatal"), new Category("HIV/AIDS"),
                new Category("Diarrhoea"), new Category("Immunization"), new Category("Others"));
    }

    public String getName() {
        return name;
    }

    public List<Commodity> getCommodities() {
        return commodities;
    }
}
