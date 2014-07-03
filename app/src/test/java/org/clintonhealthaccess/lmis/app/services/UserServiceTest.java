package org.clintonhealthaccess.lmis.app.services;

import android.database.sqlite.SQLiteOpenHelper;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.MySqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static com.j256.ormlite.table.TableUtils.createTableIfNotExists;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class UserServiceTest {
    private Dao<User, Long> userDao;

    @Before
    public void setUp() throws SQLException {
        SQLiteOpenHelper openHelper = getHelper(application, MySqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        createTableIfNotExists(connectionSource, User.class);
        userDao = createDao(connectionSource, User.class);
    }

    @Test
    public void testShouldKnowIfThereIsUserRegistered() throws Exception {

    }
}