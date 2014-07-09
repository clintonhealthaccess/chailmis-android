package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stock")
public class Stock {

    private Commodity commodity;

    @DatabaseField(id = true)
    private String commodityId;

    @DatabaseField(canBeNull = false)
    private int quantity;

    public Stock(Commodity commodity, int quantity){
        this.commodity = commodity;
        this.quantity = quantity;
    }

    public int quantity() {
        return quantity;
    }
}