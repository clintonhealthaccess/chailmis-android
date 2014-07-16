package org.clintonhealthaccess.lmis.app.models;

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;

@DatabaseTable(tableName = "commodities")
public class Commodity implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String lmisId;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private int orderDuration;

    @DatabaseField(canBeNull = false, foreign = true)
    private Category category;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<StockItem> stockItems;

    public Commodity() {
        // ormlite wants it
    }

    public Commodity(String name) {
        this.lmisId = name;
        this.name = name;
    }

    public Commodity(String id, String name) {
        this.lmisId = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getOrderDuration() {
        return orderDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commodity)) return false;

        Commodity commodity = (Commodity) o;

        return lmisId.equals(commodity.lmisId);
    }

    @Override
    public int hashCode() {
        return lmisId.hashCode();
    }

    public String getLmisId() {
        return lmisId;
    }

    //BAD
    public void setCategory(Category category) {
        this.category = category;
    }

    public StockItem getStockItem() {
        try {
            return copyOf(stockItems).get(0);
        } catch (Exception e) {
            throw new LmisException(String.format("Stock for commodity %s not found", name));
        }
    }

    public boolean stockIsFinished() {
        if (stockItems != null) {
            List<StockItem> items = ImmutableList.copyOf(stockItems);
            if (!items.isEmpty()) return items.get(0).isFinished();
        }

        return true;
    }
}
