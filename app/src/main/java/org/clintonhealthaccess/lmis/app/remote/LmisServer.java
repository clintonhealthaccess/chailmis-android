package org.clintonhealthaccess.lmis.app.remote;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.User;

import java.util.List;

public interface LmisServer {
    void validateLogin(User user);

    List<Category> fetchCommodities(User user);

    java.util.Map<String, List<String>> fetchOrderReasons(User user);
}
