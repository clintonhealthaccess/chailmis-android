package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "dispensings")
public class Dispensing implements Serializable {
    private List<DispensingItem> dispensingItems = new ArrayList<>();

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    private boolean dispenseToFacility;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date created;

    public void addItem(DispensingItem dispensingItem) {
        dispensingItems.add(dispensingItem);
    }

    public List<DispensingItem> getDispensingItems() {
        return dispensingItems;
    }

    public Dispensing() {
        dispenseToFacility = false;
        created = new Date();
    }

    public void setDispenseToFacility(boolean dispenseToFacility) {
        this.dispenseToFacility = dispenseToFacility;
    }

    public boolean isDispenseToFacility() {
        return dispenseToFacility;
    }
}
