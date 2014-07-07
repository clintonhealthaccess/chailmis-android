package org.clintonhealthaccess.lmis.app.models;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {
    private String name;
    private List<Commodity> commodities;

    public Category(String name, String... commodityNames) {
        this.name = name;
        commodities = Commodity.buildList(commodityNames);
    }

    public static List<Category> all() {
        Category antiMalarials = new Category("Anti Malarials", "Coartem", "Choloquine", "Quinine", "Fansida", "Hedex", "Septrin");
        Category antenatal = new Category("Antenatal", "Condom");
        return ImmutableList.of(
                antiMalarials, antenatal, new Category("HIV/AIDS"),
                new Category("Diarrhoea"), new Category("Immunization"), new Category("Others"));
    }

    public String getName() {
        return name;
    }

    public List<Commodity> getCommodities() {
        return commodities;
    }
}
