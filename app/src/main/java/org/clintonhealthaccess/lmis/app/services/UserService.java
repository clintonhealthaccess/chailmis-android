package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.io.IOException;
import java.sql.SQLException;

import static android.util.Base64.NO_WRAP;
import static android.util.Log.i;
import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static org.apache.http.HttpStatus.SC_OK;

public class UserService {
    private String dhis2BaseUrl = "http://104.131.225.22:8888/dhis2";

    @Inject
    private Context context;

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
        String credentials = username + ":" + password;
        String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), NO_WRAP);
        HttpGet request = new HttpGet(dhis2BaseUrl + "/api/users");
        request.addHeader("Authorization", "Basic " + base64EncodedCredentials);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            i("Failed to connect DHIS2 server.", e.getMessage());
            throw new ServiceException("Network issue. Please try again later.");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode != SC_OK) {
            i("Failed attempt to login.", "Response code : " + statusCode);
            throw new ServiceException("Please input correct username / password combination");
        }
    }

    private Dao<User, Long> initialiseDao() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, User.class);
    }
}
