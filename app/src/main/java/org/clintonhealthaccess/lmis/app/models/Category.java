package org.clintonhealthaccess.lmis.app.models;

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@DatabaseTable(tableName = "commodity_categories")
public class Category implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String lmisId;

    @DatabaseField(canBeNull = false)
    private String name;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<Commodity> commoditiesCollection;

    private List<Commodity> commodities = new ArrayList<>();

    public Category() {
        // ormlite likes it
    }

    public Category(String name) {
        this.lmisId = name;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Commodity> getCommodities() {
        if (commoditiesCollection == null) {
            return newArrayList();
        }
        return ImmutableList.copyOf(commoditiesCollection);
    }

    public List<Commodity> getNotSavedCommodities() {
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

    public void addCommodity(Commodity commodity) {
        commodities.add(commodity);
    }
}
