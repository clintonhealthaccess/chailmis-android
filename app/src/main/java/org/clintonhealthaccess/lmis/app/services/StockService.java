package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class StockService {

    @Inject
    private Context context;

    public int getStockLevelFor(Commodity commodity) {
        StockItem stockItem;
        Dao<StockItem, String> stockDao;
        try {
            stockDao = initialiseDao();
            List<StockItem> stockItems = stockDao.queryForEq(StockItem.COMMODITY_COLUMN_NAME, commodity);
            if(stockItems.size() == 1) {
                stockItem = stockItems.get(0);
            }
            else if(stockItems.size() == 0) {
                throw new LmisException(String.format("Stock for commodity %s not found", commodity));
            }
            else {
                throw new LmisException(String.format("More than one row found for commodity %s", commodity));
            }

        } catch (SQLException e) {
            throw new LmisException(e);
        }
        finally {
            releaseHelper();
        }

        return stockItem.quantity();
    }

    private Dao<StockItem, String> initialiseDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, StockItem.class);
    }
}