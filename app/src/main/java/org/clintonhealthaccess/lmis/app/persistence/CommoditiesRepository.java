package org.clintonhealthaccess.lmis.app.persistence;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.sql.SQLException;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class CommoditiesRepository {
    @Inject
    private Context context;

    @Inject
    private DbUtil dbUtil;

    public List<Category> allCategories() {
        return dbUtil.withDao(Category.class, new Operation<Category, List<Category>>() {
            @Override
            public List<Category> operate(Dao<Category, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

    public void save(final List<Category> allCategories) {
        dbUtil.withDao(Category.class, new Operation<Category, Void>() {
            @Override
            public Void operate(Dao<Category, String> dao) throws SQLException {
                for (Category category : allCategories) {
                    dao.create(category);
                    for (Commodity commodity : category.getNotSavedCommodities()) {
                        commodity.setCategory(category);
                        save(commodity);
                    }
                }
                return null;
            }
        });
    }

    private void save(final Commodity commodity) {
        dbUtil.withDao(Commodity.class, new Operation<Commodity, Void>() {
            @Override
            public Void operate(Dao<Commodity, String> dao) throws SQLException {
                dao.create(commodity);
                return null;
            }
        });
    }
}
