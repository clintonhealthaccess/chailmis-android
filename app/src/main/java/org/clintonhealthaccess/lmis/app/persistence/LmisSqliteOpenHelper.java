package org.clintonhealthaccess.lmis.app.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.ServiceException;

import java.sql.SQLException;

import static com.j256.ormlite.table.TableUtils.createTable;

public class LmisSqliteOpenHelper extends OrmLiteSqliteOpenHelper {
    public LmisSqliteOpenHelper(Context context) {
        super(context, "lmis_test_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            createTable(connectionSource, User.class);
        } catch (SQLException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
