package org.clintonhealthaccess.lmis.app.remote;

import com.google.common.base.Function;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.CategoryOptionCombos;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.DataElement;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2EndPointFactory;
import org.clintonhealthaccess.lmis.app.remote.endpoints.Dhis2Endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static android.util.Log.i;
import static com.google.common.collect.Collections2.transform;

public class Dhis2 implements LmisServer {
    @Inject
    private Dhis2EndPointFactory dhis2EndPointFactory;

    @Override
    public void validateLogin(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        service.validateLogin();
    }

    @Override
    public List<Category> fetchCommodities() {
        Dhis2Endpoint service = dhis2EndPointFactory.create(new User("android_1", "Password1"));

        DataSet dataSet = service.getDataSet("wXidpxeF08C");
        List<DataElement> elements = new ArrayList<>();
        for (DataElement element : dataSet.getDataElements()) {
            i("Element", element.getName());
            element = service.getDataElement(element.getId());
            element.setCategoryCombo(service.getCategoryCombo(element.getCategoryCombo().getId()));
            elements.add(element);
        }
        Collection<Category> cats =
                transform(elements, new Function<DataElement, Category>() {
                    @Override
                    public Category apply(DataElement element) {
                        Category cat = new Category(element.getName());
                        for (CategoryOptionCombos option : element.getCategoryCombo().getCategoryOptionCombos()) {
                            cat.addCommodity(new Commodity(option.getId(), option.getName()));
                        }
                        return cat;
                    }
                });
        return new ArrayList<>(cats);
    }

    @Override
    public Map<String, List<String>> fetchOrderReasons(User user) {
        Dhis2Endpoint service = dhis2EndPointFactory.create(user);
        return service.getReasonsForOrder();
    }
}
