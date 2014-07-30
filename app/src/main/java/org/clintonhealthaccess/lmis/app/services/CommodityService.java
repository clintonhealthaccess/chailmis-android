package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Aggregation;
import org.clintonhealthaccess.lmis.app.models.AggregationField;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.models.User;
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

    public void initialise(User user) {
        List<Category> allCommodities = lmisServer.fetchCommodities(user);
        saveToDatabase(allCommodities);
        categoryService.clearCache();
    }

    public List<Commodity> all() {
        List<Category> categories = categoryService.all();
        List<Commodity> commodities = new ArrayList<>();
        for (Category category : categories) {
            commodities.addAll(category.getCommodities());
        }
        return commodities;
    }

    protected void saveToDatabase(final List<Category> allCommodities) {
        dbUtil.withDao(Category.class, new Operation<Category, Void>() {
            @Override
            public Void operate(Dao<Category, String> dao) throws SQLException {
                for (Category category : allCommodities) {
                    dao.createOrUpdate(category);
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
                    dao.createOrUpdate(commodity);
                    createStock(commodity);
                    if (commodity.getAggregation() != null)
                        createAggregation(commodity);
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

    private void createAggregation(final Commodity commodity) {

        dbUtil.withDao(Aggregation.class, new Operation<Aggregation, Void>() {
            @Override
            public Void operate(Dao<Aggregation, String> dao) throws SQLException {
                Aggregation aggregation = commodity.getAggregation();
                dao.createOrUpdate(aggregation);
                saveAllAggregationFields(aggregation);
                return null;
            }
        });
    }

    private void saveAllAggregationFields(final Aggregation aggregation) {
        dbUtil.withDao(AggregationField.class, new Operation<AggregationField, Void>() {
            @Override
            public Void operate(Dao<AggregationField, String> dao) throws SQLException {
                for (AggregationField field : aggregation.getAggregationFields()) {
                    field.setAggregation(aggregation);
                    dao.createOrUpdate(field);
                }
                return null;
            }
        });
    }
}
