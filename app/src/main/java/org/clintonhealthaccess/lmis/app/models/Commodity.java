package org.clintonhealthaccess.lmis.app.models;

import com.google.common.base.Function;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

@DatabaseTable(tableName = "commodities")
public class Commodity implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String lmisId;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false, foreign = true)
    private Category category;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<StockItem> stockItems;

    private boolean selected;

    public Commodity() {
        // ormlite likes it
    }

    public Commodity(String name) {
        this.lmisId = name;
        this.name = name;
    }


    static List<Commodity> buildList(String[] commodityNames) {
        return copyOf(transform(copyOf(commodityNames), new Function<String, Commodity>() {
            @Override
            public Commodity apply(String name) {
                return new Commodity(name);
            }
        }));
    }

    public String getName() {
        return name;
    }

    public boolean getSelected() {
        return selected;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commodity)) return false;

        Commodity commodity = (Commodity) o;

        if (!lmisId.equals(commodity.lmisId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lmisId.hashCode();
    }

    public String getLmisId() {
        return lmisId;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public StockItem getStockItem() {
        try {
            return copyOf(stockItems).get(0);
        } catch(Exception e) {
            throw new LmisException(String.format("Stock for commodity %s not found", name));
        }
    }
}
