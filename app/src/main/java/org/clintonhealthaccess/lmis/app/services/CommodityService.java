package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

public class CommodityService {
    @Inject
    private LmisServer lmisServer;

    @Inject
    private CommoditiesRepository commoditiesRepository;

    public void initialise() {
        String commoditiesJson = lmisServer.fetchCommodities();
        commoditiesRepository.save(commoditiesJson);
    }
}
