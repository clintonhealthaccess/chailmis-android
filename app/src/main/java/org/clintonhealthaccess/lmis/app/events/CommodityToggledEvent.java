package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;

public class CommodityToggledEvent {
    private final BaseCommodityViewModel commodity;

    public CommodityToggledEvent(BaseCommodityViewModel commodity) {
        this.commodity = commodity;
    }

    public BaseCommodityViewModel getCommodity() {
        return commodity;
    }
}
