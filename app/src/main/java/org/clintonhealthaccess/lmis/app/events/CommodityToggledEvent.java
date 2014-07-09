package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.models.Commodity;

public class CommodityToggledEvent {
    public CommodityToggledEvent(Commodity commodity) {
        this.commodity = commodity;
    }

    private Commodity commodity;

    public Commodity getCommodity() {
        return commodity;
    }
}
