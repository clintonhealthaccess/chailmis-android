package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.util.List;

public class CommodityService {
    @Inject
    private LmisServer lmisServer;

    @Inject
    private CommoditiesRepository commoditiesRepository;

    public void initialise() {
        List<Category> allCommodities = lmisServer.fetchCommodities();
        commoditiesRepository.save(allCommodities);
    }
}
