package org.clintonhealthaccess.lmis.app.models;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {
    private final String id;
    private final String name;
    private final List<Commodity> commodities;

    public Category(String name, String... commodityNames) {
        this.id = name;
        this.name = name;
        commodities = Commodity.buildList(commodityNames);
    }

    public String getName() {
        return name;
    }

    public List<Commodity> getCommodities() {
        return commodities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Category)) return false;

        Category category = (Category) o;

        return !(name != null ? !name.equals(category.name) : category.name != null);

    }

    @Override
    public int hashCode() {
        return 31 * (name != null ? name.hashCode() : 0);
    }
}
