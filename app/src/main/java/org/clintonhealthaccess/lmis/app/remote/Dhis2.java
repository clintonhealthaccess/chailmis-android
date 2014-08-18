/*
 * Copyright (c) 2014, Clinton Health Access Initiative
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

import com.google.common.base.Function;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Aggregation;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.api.DataElement;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroup;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroupSet;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2EndPointFactory;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.transform;

public class Dhis2 implements LmisServer {
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
        String groupSetId = service.getDateElementGroupSetId();
        DataElementGroupSet groupSet = service.getDataElementGroupSet(groupSetId);
        List<DataElementGroup> groups = groupSet.getDataElementGroups();
        List<DataElementGroup> detailedElementGroups = new ArrayList<>();
        for (DataElementGroup group : groups) {
            detailedElementGroups.add(getDataElementGroupDetails(service, group));
        }
        return transformDataElementGroupsToCategories(detailedElementGroups);
    }

    private DataElementGroup getDataElementGroupDetails(Dhis2Endpoint service, DataElementGroup group) {
        List<DataElement> fetchedElements = new ArrayList<>();
        DataElementGroup fetchedGroup = service.getDataElementGroup(group.getId());
        for (DataElement element : fetchedGroup.getDataElements()) {
            fetchedElements.add(getDataElementDetails(service, element));
        }
        fetchedGroup.getDataElements().clear();
        fetchedGroup.getDataElements().addAll(fetchedElements);
        return fetchedGroup;
    }

    private DataElement getDataElementDetails(Dhis2Endpoint service, DataElement element) {
        DataElement dataElementDetail = service.getDataElement(element.getId());
        Aggregation aggregationDetail = service.getCategoryCombo(dataElementDetail.getAggregation().getId());
        dataElementDetail.setAggregation(aggregationDetail);
        return dataElementDetail;
    }

    @Override
    public Map<String, List<String>> fetchOrderReasons(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.getReasonsForOrder();
    }

    private List<Category> transformDataElementGroupsToCategories(List<DataElementGroup> groups) {
        return transform(groups, new Function<DataElementGroup, Category>() {
            @Override
            public Category apply(DataElementGroup group) {
                Category category = new Category(group.getName());
                for (DataElement element : group.getDataElements()) {
                    Aggregation aggregation = element.getAggregation();
                    Commodity commodity = new Commodity(element.getId(), element.getName(), aggregation);
                    category.addCommodity(commodity);
                    if (element.getDataSets() != null){
                        category.setDataSet(element.getDataSets().get(0));
                    }
                }
                return category;
            }
        });
    }

}
