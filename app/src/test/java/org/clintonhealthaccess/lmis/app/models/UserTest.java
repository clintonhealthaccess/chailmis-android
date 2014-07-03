package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static com.j256.ormlite.table.TableUtils.clearTable;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class UserTest {
    private Dao<User, Long> userDao;
    private AndroidConnectionSource connectionSource;
    private LmisSqliteOpenHelper openHelper;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);

        openHelper = getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        userDao = createDao(connectionSource, User.class);
    }

    @After
    public void tearDown() throws Exception {
        clearTable(connectionSource, User.class);
        releaseHelper();
    }

    @Test
    public void testShouldBeAbleToCRUD() throws Exception {
        assertThat(userDao.countOf(), is(0l));

        User user = new User("admin", "district");
        int newUserId = userDao.create(user);
        assertThat(newUserId, is(1));
        assertThat(userDao.countOf(), is(1l));
    }
}