package org.clintonhealthaccess.lmis.app.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class DbUtil {
    public interface Operation<DomainType, ReturnType> {
        ReturnType operate(Dao<DomainType, String> dao) throws SQLException;
    }

    @Inject
    private Context context;

    public <DomainType, ReturnType> ReturnType withDao(
            Class<DomainType> domainClass, Operation<DomainType, ReturnType> operation) {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            Dao<DomainType, String> dao = initialiseDao(openHelper, domainClass);
            return operation.operate(dao);
        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
        }
    }

    private <T> Dao<T, String> initialiseDao(SQLiteOpenHelper openHelper, Class<T> domainClass) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, domainClass);
    }
}
