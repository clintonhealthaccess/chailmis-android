package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;


public class StockService {
    @Inject
    private DbUtil dbUtil;

    @Inject
    private CategoryService categoryService;


    public int getStockLevelFor(Commodity commodity) {
        return commodity.getStockOnHand();
    }

    public void reduceStockLevelFor(final Commodity commodity, int quantity) {
        commodity.reduceStockOnHandBy(quantity);
        saveStockLevel(commodity);
        categoryService.clearCache();
    }

    public void increaseStockLevelFor(Commodity commodity, int quantity) {
        commodity.increaseStockOnHandBy(quantity);
        saveStockLevel(commodity);
        categoryService.clearCache();
    }

    private void saveStockLevel(final Commodity commodity) {
        dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                dao.update(commodity.getStockItem());
                return null;
            }
        });
    }
}