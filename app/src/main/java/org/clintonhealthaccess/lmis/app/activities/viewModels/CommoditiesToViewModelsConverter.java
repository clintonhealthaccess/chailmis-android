package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.io.Serializable;
import java.util.List;

public interface CommoditiesToViewModelsConverter extends Serializable {
    public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities);
}
