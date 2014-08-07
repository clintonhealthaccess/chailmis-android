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

    @DatabaseField(canBeNull = false)
    private int wastages;

    @DatabaseField(canBeNull = false)
    private int missing;

    @DatabaseField(canBeNull = false)
    private int damages;

    @DatabaseField(canBeNull = false)
    private int expiries;

    public LossItem(Commodity commodity) {
        this.commodity = commodity;
    }

    public LossItem(Commodity commodity, int expiries, int damages) {
        this(commodity);
        this.expiries = expiries;
        this.damages = damages;
    }

    public int getNewStockOnHand() {
        return commodity.getStockOnHand() - getTotalLosses();
    }

    public int getTotalLosses() {
        return missing + wastages + damages + expiries;
    }
}
