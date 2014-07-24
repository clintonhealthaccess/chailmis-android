package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;

import org.clintonhealthaccess.lmis.app.services.OrderItemSaver;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Order {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String srvNumber = "DUMMY ORDER ID NUMBER";

    private List<OrderItem> orderItems = newArrayList();

    public Order() {
    }

    public Order(String srvNumber) {
        this.srvNumber = srvNumber;
    }

    public void addItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public boolean has(OrderItem item) {
        return orderItems.contains(item);
    }

    @Override
    public boolean equals(Object otherOrder) {
        if (this == otherOrder) return true;
        if (otherOrder == null || getClass() != otherOrder.getClass()) return false;

        for (OrderItem item : this.orderItems) {
            if (!((Order) otherOrder).has(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = srvNumber != null ? srvNumber.hashCode() : 0;
        result = 31 * result + orderItems.hashCode();
        return result;
    }

    public String getSrvNumber() {
        return srvNumber;
    }

    public void saveOrderItems(OrderItemSaver saver) {
        for (OrderItem item : orderItems) {
            item.setOrder(this);
            saver.saveOrderItem(item);
        }
    }
}
