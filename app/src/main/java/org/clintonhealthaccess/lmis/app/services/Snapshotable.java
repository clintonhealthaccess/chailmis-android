package org.clintonhealthaccess.lmis.app.services;

import org.clintonhealthaccess.lmis.app.models.AggregationField;
import org.clintonhealthaccess.lmis.app.models.Commodity;

public interface Snapshotable {
    Commodity getCommodity();
    AggregationField getAggregationField();
    int getValue();
}
