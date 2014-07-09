package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "dispensings")
public class Dispensing implements Serializable {
    private List<DispensingItem> dispensingItems = new ArrayList<>();

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    private boolean dispenseToFacility;

    public void addItem(DispensingItem dispensingItem) {
        dispensingItems.add(dispensingItem);
    }

    public List<DispensingItem> getDispensingItems() {
        return dispensingItems;
    }

    public Dispensing() {
        dispenseToFacility = false;
    }
}
