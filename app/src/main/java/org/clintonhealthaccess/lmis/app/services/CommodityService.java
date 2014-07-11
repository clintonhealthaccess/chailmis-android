package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class CommodityService {
    @Inject
    private LmisServer lmisServer;

    @Inject
    private CategoryService categoryService;

    @Inject
    private DbUtil dbUtil;

    public void initialise() {
        List<Category> allCommodities = lmisServer.fetchCommodities();
        saveToDatabase(allCommodities);
        categoryService.clearCache();
    }

    public List<Commodity> all() {
        List<Category> categories = categoryService.all();
        List<Commodity> commodities = new ArrayList<>();
        for(Category category : categories) {
            commodities.addAll(category.getCommodities());
        }
        return commodities;
    }

    private void saveToDatabase(final List<Category> allCommodities) {
        dbUtil.withDao(Category.class, new Operation<Category, Void>() {
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
        dbUtil.withDao(Commodity.class, new Operation<Commodity, Void>() {
            @Override
            public Void operate(Dao<Commodity, String> dao) throws SQLException {
                for (Commodity commodity : category.getNotSavedCommodities()) {
                    commodity.setCategory(category);
                    dao.create(commodity);
                    createStock(commodity);
                }
                return null;
            }
        });
    }

    private void createStock(final Commodity commodity) {
        dbUtil.withDao(StockItem.class, new Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                // FIXME: Should fetch stock levels from 'Special Receive' screen or from DHIS2
                StockItem stockItem = new StockItem(commodity, 10);
                dao.create(stockItem);
                return null;
            }
        });
    }
}
