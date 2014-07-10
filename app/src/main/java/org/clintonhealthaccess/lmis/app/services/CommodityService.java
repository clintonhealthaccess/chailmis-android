package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.List;

public class CommodityService {
    @Inject
    private LmisServer lmisServer;

    @Inject
    private DbUtil dbUtil;

    public void initialise() {
        List<Category> allCommodities = lmisServer.fetchCommodities();
        saveToDatabase(allCommodities);
    }

    private void saveToDatabase(final List<Category> allCommodities) {
        dbUtil.withDao(Category.class, new DbUtil.Operation<Category, Void>() {
            @Override
            public Void operate(Dao<Category, String> dao) throws SQLException {
                for (Category category : allCommodities) {
                    dao.create(category);
                    saveAllCommodities(category);
                }
                return null;
            }
        });
    }

    private void saveAllCommodities(final Category category) {
        dbUtil.withDao(Commodity.class, new DbUtil.Operation<Commodity, Void>() {
            @Override
            public Void operate(Dao<Commodity, String> dao) throws SQLException {
                for (Commodity commodity : category.getNotSavedCommodities()) {
                    commodity.setCategory(category);
                    dao.create(commodity);
                }
                return null;
            }
        });
    }
}
