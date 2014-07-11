package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.List;


public class StockService {

    @Inject
    private DbUtil dbUtil;
    @Inject
    CommodityService commodityService;

    public int getStockLevelFor(Commodity commodity) {
        StockItem stockItem;
        try {
            stockItem = getStockItem(commodity);
        } catch (SQLException e) {
            throw new LmisException(e);
        }

        return stockItem.quantity();
    }

    private StockItem getStockItem(final Commodity commodity) throws SQLException {
        List<StockItem> stockItems = dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, List<StockItem>>() {
            @Override
            public List<StockItem> operate(Dao<StockItem, String> dao) throws SQLException {
                return dao.queryForEq(StockItem.COMMODITY_COLUMN_NAME, commodity);
            }
        });

        StockItem stockItem;
        if (stockItems.size() == 1) {
            stockItem = stockItems.get(0);
        } else {
            throw new LmisException(String.format("Stock for commodity %s not found", commodity));
        }

        return stockItem;
    }

    public void initialise() {
        for (final Commodity commodity : commodityService.all()) {
            dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
                @Override
                public Void operate(Dao<StockItem, String> dao) throws SQLException {
                    StockItem stockItem = new StockItem(commodity, nextStockLevel());
                    dao.create(stockItem);
                    return null;
                }
            });
        }
    }

    public void updateStockLevelFor(final Commodity commodity, int quantity) {
        try {
            final StockItem stockItem = getStockItem(commodity);
            stockItem.reduceQuantityBy(quantity);
            dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
                @Override
                public Void operate(Dao<StockItem, String> dao) throws SQLException {
                    dao.update(stockItem);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LmisException(e);
        }

    }

    private int previous = 0;

    private int nextStockLevel() {
        // FIXME: Should fetch stock levels from 'Special Receive' screen or form DHIS2
        if (previous == 0) {
            previous = 10;
            return previous;
        } else {
            previous = 0;
            return previous;
        }
    }
}