package org.clintonhealthaccess.lmis.app.models;

import java.io.Serializable;
import java.util.List;

public class Dispensing implements Serializable {
    List<DispensingItem> dispensingItems;

    public void addItem(DispensingItem dispensingItem) {
        dispensingItems.add(dispensingItem);
    }
}
