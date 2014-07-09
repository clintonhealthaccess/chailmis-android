package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class DispensingService {
    @Inject
    private Context context;

    private Dao<Dispensing, Long> initialiseDispensingDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Dispensing.class);
    }

    public void addDispensing(Dispensing dispensing) {
        try {
            Dao<DispensingItem, Long> dispensingDao = initialiseDispensingItemDao();
            saveDispensing(dispensing);
            for (DispensingItem item : dispensing.getDispensingItems()) {
                item.setDispensing(dispensing);
                dispensingDao.create(item);
            }
        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
        }
    }

    public List<DispensingItem> getAllDispensingItems() {
        try {
            Dao<DispensingItem, Long> dispensingDao = initialiseDispensingItemDao();
            return dispensingDao.queryForAll();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseHelper();
        }

        return null;

    }


    private Dao<DispensingItem, Long> initialiseDispensingItemDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, DispensingItem.class);
    }

    private void saveDispensing(Dispensing dispensing) throws SQLException {
        try {
            Dao<Dispensing, Long> dispensingDao = initialiseDispensingDao();
            dispensingDao.create(dispensing);
        } finally {
            releaseHelper();
        }


    }
}
