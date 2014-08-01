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
                    if (!element.getDataSets().isEmpty()){
                    category.setDataSet(element.getDataSets().get(0));
                    }
                }
                return category;
            }
        });
    }

}
