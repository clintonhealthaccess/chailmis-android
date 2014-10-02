package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@DatabaseTable(tableName = "loss_item_details")
public class LossItemDetail implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LossItem lossItem;

    @DatabaseField(canBeNull = false)
    private int value;

    @DatabaseField(canBeNull = false)
    private String reason;

    public LossItemDetail(LossItem lossItem, LossReason reason) {
        this.lossItem = lossItem;
        this.reason = reason.name();
        this.value = 0;
    }
}
