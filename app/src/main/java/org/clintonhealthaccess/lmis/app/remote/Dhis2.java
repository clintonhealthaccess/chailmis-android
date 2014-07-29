package org.clintonhealthaccess.lmis.app.remote;

import com.google.common.base.Function;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.api.CategoryCombo;
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
        List<DataElementGroup> fetchedGroups = new ArrayList<>();
        for (DataElementGroup group : groups) {
            DataElementGroup fetchedGroup = service.getDataElementGroup(group.getId());
            List<DataElement> fetchedElements = new ArrayList<>();
            for (DataElement element : fetchedGroup.getDataElements()) {
                DataElement fetchedElement = service.getDataElement(element.getId());
                CategoryCombo combo = service.getCategoryCombo(fetchedElement.getCategoryCombo().getId());
                fetchedElement.setCategoryCombo(combo);
                fetchedElements.add(fetchedElement);
            }
            fetchedGroup.getDataElements().clear();
            fetchedGroup.getDataElements().addAll(fetchedElements);
            fetchedGroups.add(fetchedGroup);
        }
        return transformDataElementGroupsToCategories(fetchedGroups);
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
                Category cat = new Category(group.getName());
                for (DataElement element : group.getDataElements()) {
                    cat.addCommodity(new Commodity(element.getId(), element.getName()));
                    //FIXME store the combo options
                }
                return cat;
            }
        });
    }

}
