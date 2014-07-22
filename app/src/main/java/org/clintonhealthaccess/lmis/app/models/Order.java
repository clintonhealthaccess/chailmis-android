package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.List;

public class Order {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    private String srvNumber = "DUMMY ORDER ID NUMBER";

    @ForeignCollectionField(eager = true)
    private ForeignCollection<OrderItem> orderItems;

    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public void addItem(OrderItem orderItem) {
        items.add(orderItem);
    }

    public boolean has(OrderItem item) {
        return items.contains(item);
    }

    @Override
    public boolean equals(Object otherOrder) {
        if (this == otherOrder) return true;
        if (otherOrder == null || getClass() != otherOrder.getClass()) return false;

        for(OrderItem item : this.items) {
            if(!((Order)otherOrder).has(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = srvNumber != null ? srvNumber.hashCode() : 0;
        result = 31 * result + items.hashCode();
        return result;
    }
}
