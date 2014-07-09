package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static com.j256.ormlite.table.TableUtils.clearTable;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class DispensingServiceTest {
    @Inject
    DispensingService dispensingService;
    private LmisSqliteOpenHelper openHelper;
    private Dao<DispensingItem, Long> dispensingItemDao;
    private AndroidConnectionSource connectionSource;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);

        openHelper = getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        dispensingItemDao = createDao(connectionSource, DispensingItem.class);
    }

    @After
    public void tearDown() throws Exception {
        clearTable(connectionSource, User.class);
        releaseHelper();
    }

    @Test
    public void testSaveDispensingSavesItsDispensingItems() throws Exception {

        DispensingItem item1 = new DispensingItem(new Commodity("food"), 1);
        DispensingItem item2 = new DispensingItem(new Commodity("greens"), 1);

        Dispensing dispensing = new Dispensing();

        dispensing.addItem(item1);
        dispensing.addItem(item2);

        dispensingService.addDispensing(dispensing);

        assertThat(dispensingItemDao.countOf(), is(2L));


    }


}