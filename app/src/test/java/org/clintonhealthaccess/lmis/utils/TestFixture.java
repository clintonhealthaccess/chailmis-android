package org.clintonhealthaccess.lmis.utils;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static java.util.Arrays.asList;

public class TestFixture {
    public static void initialiseDefaultCommodities(Context context) throws IOException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            for (Category category : defaultCategories(context)) {
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

    public static List<Category> defaultCategories(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        String defaultCommoditiesAsJson = CharStreams.toString(new InputStreamReader(src));
        return asList(new Gson().fromJson(defaultCommoditiesAsJson, Category[].class));
    }

    public static List<Commodity> getDefaultCommodities(Context context) throws IOException {
        List<Commodity> defaultCommodities = new ArrayList<>();
        for(Category category : defaultCategories(context)) {
            defaultCommodities.addAll(category.getCommodities());
        }
        return defaultCommodities;
    }

    private static Dao<Category, String> initialiseCategoryDao(SQLiteOpenHelper openHelper) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Category.class);
    }

    private static Dao<Commodity, String> initialiseCommodityDao(SQLiteOpenHelper openHelper) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Commodity.class);
    }

}
