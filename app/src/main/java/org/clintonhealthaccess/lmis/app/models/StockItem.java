package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "stock")
public class StockItem implements Serializable {
    public StockItem() {
        // don't delete. ormlite likes it.
    }

    @DatabaseField(generatedId = true)
    protected int id;

    @DatabaseField(unique = true, foreign = true, canBeNull = false)
    protected Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;

    public StockItem(Commodity commodity, int quantity) {
        this.commodity = commodity;
        this.quantity = quantity;
    }

    public int quantity() {
        return quantity;
    }

    public void reduceQuantityBy(int quantityToReduceBy) {
        quantity -= quantityToReduceBy;
    }
}
