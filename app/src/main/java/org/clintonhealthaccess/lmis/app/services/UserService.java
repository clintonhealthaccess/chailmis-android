package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class UserService {
    @Inject
    private Context context;
    @Inject
    private LmisServer lmisServer;

    public boolean userRegistered() {
        try {
            Dao<User, Long> userDao = initialiseDao();
            return userDao.countOf() > 0;
        } catch (SQLException e) {
            throw new ServiceException(e);
        } finally {
            releaseHelper();
        }
    }

    public User register(String username, String password) {
        lmisServer.validateLogin(username, password);

        User user = new User(username, password);
        try {
            Dao<User, Long> userDao = initialiseDao();
            userDao.create(user);
        } catch (SQLException e) {
            throw new ServiceException(e);
        } finally {
            releaseHelper();
        }
        return user;
    }

    private Dao<User, Long> initialiseDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, User.class);
    }
}
