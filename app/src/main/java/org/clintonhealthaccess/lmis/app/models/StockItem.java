package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stock")
public class StockItem {

    public StockItem() {}

    @DatabaseField(id = true)
    protected String commodityId;

    @DatabaseField(canBeNull = false)
    private int quantity;

    public StockItem(Commodity commodity, int quantity) {
        commodityId = commodity.getId();
        this.quantity = quantity;
    }

    public int quantity() {
        return quantity;
    }
    public String getId() {return commodityId; }
}
