package org.clintonhealthaccess.lmis.app.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.sql.SQLException;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class CommoditiesRepository {
    @Inject
    private Context context;

    public static final String COMMODITIES_FILE = "commodities.json";

    public List<Category> allCategories() {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            return initialiseCategoryDao(openHelper).queryForAll();
        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
        }
    }

    public void save(List<Category> allCategories) {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            for (Category category : allCategories) {
                initialiseCategoryDao(openHelper).create(category);
                for (Commodity commodity : category.getNotSavedCommodities()) {
                    commodity.setCategory(category);
                    initialiseCommodityDao(openHelper).create(commodity);
                }
            }
        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
        }
    }

    private Dao<Category, String> initialiseCategoryDao(SQLiteOpenHelper openHelper) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Category.class);
    }

    private Dao<Commodity, String> initialiseCommodityDao(SQLiteOpenHelper openHelper) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Commodity.class);
    }

}
