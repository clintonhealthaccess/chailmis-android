package org.clintonhealthaccess.lmis.app.models;

import android.database.sqlite.SQLiteOpenHelper;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.utils.MySqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static com.j256.ormlite.table.TableUtils.createTableIfNotExists;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class UserTest {
    @Test
    public void testShouldBeAbleToCRUD() throws Exception {
        SQLiteOpenHelper openHelper = getHelper(application, MySqliteOpenHelper.class);
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        createTableIfNotExists(connectionSource, User.class);

        Dao<User, Long> userDao = createDao(connectionSource, User.class);
        assertThat(userDao.countOf(), is(0l));

        User user = new User("admin", "district");
        int newUserId = userDao.create(user);
        assertThat(newUserId, is(1));
        assertThat(userDao.countOf(), is(1l));
    }
}