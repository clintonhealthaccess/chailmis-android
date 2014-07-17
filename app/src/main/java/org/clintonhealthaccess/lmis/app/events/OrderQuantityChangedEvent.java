package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;

public class OrderQuantityChangedEvent {
    private CommodityViewModel commodityViewModel;
    private int quantity;

    public OrderQuantityChangedEvent(int quantity, CommodityViewModel commodityViewModel) {
        this.commodityViewModel = commodityViewModel;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }


    public CommodityViewModel getCommodityViewModel() {
        return commodityViewModel;
    }
}
