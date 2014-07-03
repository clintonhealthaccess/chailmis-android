package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static android.util.Log.i;
import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.client.Invocation.Builder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;

public class UserService {
    private Provider<Context> contextProvider;
    private String dhis2BaseUrl = "http://104.131.225.22:8888/dhis2";

    void setDhis2BaseUrl(String dhis2BaseUrl) {
        this.dhis2BaseUrl = dhis2BaseUrl;
    }

    @Inject
    public UserService(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

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
        verifyDhisLogin(username, password);

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

    private void verifyDhisLogin(String username, String password) {
        Client client = newClient().register(basic(username, password));
        Builder request = client.target(dhis2BaseUrl + "/api/users").request(APPLICATION_JSON_TYPE);
        Response response = request.get();
        if (response.getStatus() != 200) {
            i("Failed attempt to login.", "Response code : " + response.getStatus());
            throw new ServiceException("Please input correct username / password combination");
        }
    }

    private Dao<User, Long> initialiseDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(contextProvider.get(), LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, User.class);
    }
}
