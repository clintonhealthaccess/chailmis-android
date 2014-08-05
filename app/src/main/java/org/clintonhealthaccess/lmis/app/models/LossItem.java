package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "loss_items")
public class LossItem implements Serializable {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Loss loss;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private int wastages;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private int missing;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private int damages;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private int expiries;

    public int getNewStockOnHand() {
        return commodity.getStockOnHand() - getTotalLosses();
    }

    public int getTotalLosses() {
        return missing + wastages + damages + expiries;
    }
}
