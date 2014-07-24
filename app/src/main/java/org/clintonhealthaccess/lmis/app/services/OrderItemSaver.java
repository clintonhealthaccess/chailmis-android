package org.clintonhealthaccess.lmis.app.services;

import org.clintonhealthaccess.lmis.app.models.OrderItem;

public interface OrderItemSaver {
    void saveOrderItem(OrderItem item);
}
