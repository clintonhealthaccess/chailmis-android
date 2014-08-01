package org.clintonhealthaccess.lmis.app.models;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Collections2.filter;

@DatabaseTable(tableName = "dispensingItems")
public class DispensingItem implements Serializable, Snapshotable {
    public static final String DISPENSE = "dispense";
    Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false)
    private String commodityId;

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, foreign = true)
    private Dispensing dispensing;

    @DatabaseField
    private Date created;

    public DispensingItem(Commodity commodity, int quantity) {
        this.commodity = commodity;
        this.commodityId = commodity.getLmisId();
        this.quantity = quantity;
        created = new Date();
    }

    public DispensingItem() {
        created = new Date();
    }

    public Commodity getCommodity() {
        return commodity;
    }

    @Override
    public AggregationField getAggregationField() {
        List<AggregationField> fields = ImmutableList.copyOf(getCommodity().getAggregation().getAggregationFieldsCollection());
        Collection<AggregationField> filteredFields = filter(fields, new Predicate<AggregationField>() {
            @Override
            public boolean apply(AggregationField input) {
                return input.getName().toLowerCase().contains(DISPENSE);
            }
        });
        return new ArrayList<>(filteredFields).get(0);
    }

    @Override
    public int getValue() {
        return quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setDispensing(Dispensing dispensing) {
        this.dispensing = dispensing;
    }
}
