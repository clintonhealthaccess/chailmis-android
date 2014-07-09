package org.clintonhealthaccess.lmis.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dispensing implements Serializable {
    private List<DispensingItem> dispensingItems = new ArrayList<>();

    public void addItem(DispensingItem dispensingItem) {
        dispensingItems.add(dispensingItem);
    }

    public List<DispensingItem> getDispensingItems() {
        return dispensingItems;
    }
}
