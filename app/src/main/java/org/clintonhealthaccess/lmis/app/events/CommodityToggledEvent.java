package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;

public class CommodityToggledEvent {
    private final CommodityViewModel commodity;

    public CommodityToggledEvent(CommodityViewModel commodity) {
        this.commodity = commodity;
    }

    public CommodityViewModel getCommodity() {
        return commodity;
    }
}
