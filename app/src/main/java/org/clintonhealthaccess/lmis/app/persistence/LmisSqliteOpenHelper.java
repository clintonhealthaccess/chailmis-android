package org.clintonhealthaccess.lmis.app.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.models.User;

import java.sql.SQLException;

import static com.j256.ormlite.table.TableUtils.createTableIfNotExists;

public class LmisSqliteOpenHelper extends OrmLiteSqliteOpenHelper {
    public LmisSqliteOpenHelper(Context context) {
        super(context, "lmis_test_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            createTableIfNotExists(connectionSource, Category.class);
            createTableIfNotExists(connectionSource, Commodity.class);
            createTableIfNotExists(connectionSource, User.class);
            createTableIfNotExists(connectionSource, Dispensing.class);
            createTableIfNotExists(connectionSource, DispensingItem.class);
            createTableIfNotExists(connectionSource, StockItem.class);
            createTableIfNotExists(connectionSource, OrderReason.class);
        } catch (SQLException e) {
            throw new LmisException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
