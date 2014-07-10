package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class DispensingService {
    @Inject
    private Context context;

    @Inject
    private DbUtil dbUtil;

    private Dao<Dispensing, Long> initialiseDispensingDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Dispensing.class);
    }

    public void addDispensing(final Dispensing dispensing) {
        dbUtil.withDao(DispensingItem.class, new DbUtil.Operation<DispensingItem, DispensingItem>() {
            @Override
            public DispensingItem operate(Dao<DispensingItem, String> dao) throws SQLException {
                saveDispensing(dispensing);
                for (DispensingItem item : dispensing.getDispensingItems()) {
                    item.setDispensing(dispensing);
                    dao.create(item);
                }
                return null;
            }
        });
    }


    private void saveDispensing(final Dispensing dispensing) throws SQLException {
        dbUtil.withDao(Dispensing.class, new DbUtil.Operation<Dispensing, Dispensing>() {
            @Override
            public Dispensing operate(Dao<Dispensing, String> dao) throws SQLException {
                dao.create(dispensing);
                return dispensing;
            }
        });


    }
}
