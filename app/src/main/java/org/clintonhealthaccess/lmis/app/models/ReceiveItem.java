package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "receive_items")
public class ReceiveItem {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Receive receive;

    @DatabaseField(canBeNull = false)
    private int quantityAllocated;

    @DatabaseField(canBeNull = false)
    private int quantityReceived;

    public ReceiveItem(Commodity commodity, int quantityAllocated, int quantityReceived) {
        this.commodity = commodity;
        this.quantityAllocated = quantityAllocated;
        this.quantityReceived = quantityReceived;
    }

    public int getDifference() {
        return quantityAllocated - quantityReceived;
    }

}
