package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stock")
public class StockItem {

    public static final String COMMODITY_COLUMN_NAME = "commodity";

    public StockItem() {}

    @DatabaseField(unique = true, foreign = true, columnName = COMMODITY_COLUMN_NAME)
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
    //TODO Remove this.
    public String getId() {return commodity.getLmisId(); }
}
