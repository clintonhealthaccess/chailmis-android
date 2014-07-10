package org.clintonhealthaccess.lmis.app.remote;

import org.clintonhealthaccess.lmis.app.models.Category;

import java.util.List;

public interface LmisServer {
    void validateLogin(String username, String password);

    List<Category> fetchCommodities();
}
