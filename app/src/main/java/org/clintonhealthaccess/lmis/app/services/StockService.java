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

    public int getStockLevelFor(Commodity commodity) {
        return commodity.getStockItem().quantity();
    }

    public void updateStockLevelFor(final Commodity commodity, int quantity) {
        final StockItem stockItem = commodity.getStockItem();
        stockItem.reduceQuantityBy(quantity);
        dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                dao.update(stockItem);
                return null;
            }
        });
    }
}