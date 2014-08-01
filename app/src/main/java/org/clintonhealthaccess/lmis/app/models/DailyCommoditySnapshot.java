package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable
public class DailyCommoditySnapshot {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, uniqueCombo = true, foreign = true)
    private AggregationField aggregationField;

    @DatabaseField(canBeNull = false)
    private int value;

    @DatabaseField(defaultValue = "false")
    private boolean synced;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private Date date;

    public DailyCommoditySnapshot(Commodity commodity, AggregationField aggregationField, int value) {
        this.commodity = commodity;
        this.aggregationField = aggregationField;
        this.value = value;
        this.synced = false;
        this.date = new Date();
    }

    public void incrementValue(int value) {
        this.value += value;
    }
}
