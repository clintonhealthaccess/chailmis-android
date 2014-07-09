package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "dispensingItems")
public class DispensingItem implements Serializable {
    Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false)
    private String commodityId;

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, foreign = true)
    private Dispensing dispensing;

    public DispensingItem(Commodity commodity, int quantity) {
        this.commodity = commodity;
        this.commodityId = commodity.getId();
        this.quantity = quantity;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setDispensing(Dispensing dispensing) {
        this.dispensing = dispensing;
    }

    public DispensingItem() {
    }
}
