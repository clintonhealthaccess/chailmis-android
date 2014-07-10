package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;

import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class UserService {
    @Inject
    private Context context;

    @Inject
    private DbUtil dbUtil;

    @Inject
    private LmisServer lmisServer;

    public boolean userRegistered() {
        return dbUtil.withDao(User.class, new Operation<User, Boolean>() {
            @Override
            public Boolean operate(Dao<User, String> dao) throws SQLException {
                return dao.countOf() > 0;
            }
        });
    }

    public User register(final String username, final String password) {
        lmisServer.validateLogin(username, password);
        return saveUserToDatabase(username, password);
    }

    private User saveUserToDatabase(final String username, final String password) {
        return dbUtil.withDao(User.class, new Operation<User, User>() {
            @Override
            public User operate(Dao<User, String> dao) throws SQLException {
                User user = new User(username, password);
                dao.create(user);
                return user;
            }
        });
    }
}
