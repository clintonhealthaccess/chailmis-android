/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.remote;

import android.util.Log;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActivity;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.api.AttributeValue;
import org.clintonhealthaccess.lmis.app.models.api.DataElement;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroup;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroupSet;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2EndPointFactory;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;
import org.clintonhealthaccess.lmis.app.remote.responses.DataSetSearchResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dhis2 implements LmisServer {
    public static final String SYNC = "SYNC";
    @Inject
    private Dhis2EndPointFactory dhis2EndPointFactory;

    @Override
    public UserProfile validateLogin(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.validateLogin();
    }

    @Override
    public List<Category> fetchCommodities(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        DataSetSearchResponse response = service.searchDataSets("LMIS", "id,name,dataElements[name,id,attributeValues[value,attribute[id,name]],dataElementGroups[id,name,dataElementGroupSet[id,name]");
        return getCategoriesFromDataSets(response.getDataSets());
    }

    private List<Category> getCategoriesFromDataSets(List<DataSet> dataSets) {
        List<Category> categories = new ArrayList<>();
        List<Commodity> commodities = new ArrayList<>();
        List<DataElement> elements = new ArrayList<>();

        for (DataSet dataSet : dataSets) {
            Log.e(SYNC, String.format("DataSet: %s", dataSet.getName()));
            if (dataSet != null && dataSet.getDataElements() != null)
                elements.addAll(dataSet.getDataElements());
        }

        for (DataElement element : elements) {
            Log.e(SYNC, String.format("DatElement: %s", element.getName()));
            getOrCreateCommodity(element, commodities, categories);

        }

        return categories;
    }

    private void getOrCreateCommodity(DataElement element, List<Commodity> commodities, List<Category> categories) {
        Commodity commodity = new Commodity();
        Commodity actualCommodity;
        if (element.getDataElementGroups().size() > 0) {
            DataElementGroup dataElementGroup = element.getDataElementGroups().get(0);
            DataElementGroupSet dataElementGroupSet = dataElementGroup.getDataElementGroupSet();
            commodity.setId(dataElementGroup.getId());
            if (commodities.contains(commodity)) {
                actualCommodity = commodities.get(commodities.indexOf(commodity));
            } else {
                commodity.setName(dataElementGroup.getName());
                Category category = new Category();
                category.setName(dataElementGroupSet.getName());
                category.setLmisId(dataElementGroupSet.getId());
                if (categories.contains(category)) {
                    List<Commodity> updatedCommodities = categories.get(categories.indexOf(category)).getNotSavedCommodities();
                    updatedCommodities.add(commodity);
                    category.setCommodities(updatedCommodities);
                    int location = categories.indexOf(category);
                    categories.set(location, category);
                } else {
                    category.setCommodities(new ArrayList<Commodity>(Arrays.asList(commodity)));
                    categories.add(category);
                }
                commodity.setCommodityActivities(new ArrayList<CommodityActivity>());
                commodities.add(commodity);
                actualCommodity = commodity;
            }
            if (element.getAttributeValues().size() > 0) {
                AttributeValue attributeValue = element.getAttributeValues().get(0);
                CommodityActivity commodityActivity = new CommodityActivity(actualCommodity, element.getId(), element.getName(), attributeValue.getValue());
                actualCommodity.getCommodityActivities().add(commodityActivity);
            }
        }


    }

    @Override
    public Map<String, List<String>> fetchOrderReasons(User user) {
        Map<String, List<String>> reasons = new HashMap<>();
        reasons.put("order_reasons", Arrays.asList("Emergency", "Routine"));
        reasons.put("unexpected_quantity_reasons", Arrays.asList("Losses", "Expiries", "High Demand"));
        return reasons;
    }

}
