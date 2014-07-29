package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;

public class OrderQuantityChangedEvent {
    private OrderCommodityViewModel commodityViewModel;
    private int quantity;

    public OrderQuantityChangedEvent(int quantity, OrderCommodityViewModel commodityViewModel) {
        this.commodityViewModel = commodityViewModel;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }


    public OrderCommodityViewModel getCommodityViewModel() {
        return commodityViewModel;
    }
}
