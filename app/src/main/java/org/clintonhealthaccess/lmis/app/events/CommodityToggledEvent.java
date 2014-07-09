package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.models.Commodity;

public class CommodityToggledEvent {
    private final Commodity commodity;

    public CommodityToggledEvent(Commodity commodity) {
        this.commodity = commodity;
    }

    public Commodity getCommodity() {
        return commodity;
    }
}
