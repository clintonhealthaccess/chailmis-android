package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
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
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.addPendingHttpResponse;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderServiceTest {

    @Inject
    private OrderService orderService;

    @Inject
    private DbUtil dbUtil;

    private UserService mockUserService;
    private LmisSqliteOpenHelper openHelper;
    private AndroidConnectionSource connectionSource;
    private Dao<OrderReason, ?> reasonDao;

    @Before
    public void setUp() throws SQLException {
        mockUserService = mock(UserService.class);
        when(mockUserService.getRegisteredUser()).thenReturn(new User("", ""));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });

        openHelper = getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        reasonDao = createDao(connectionSource, OrderReason.class);
    }

    @Test
    public void shouldGetReasonsForOrderFromDHIS2AndSaveThem() throws Exception {
        addPendingHttpResponse(200, "['reason 1','reason 2']");

        orderService.syncReasons();

        assertThat(reasonDao.countOf(), is(2L));

        addPendingHttpResponse(200, "['reason 1','reason 2']");

        orderService.syncReasons();

        assertThat(reasonDao.countOf(), is(2L));
    }


    @After
    public void tearDown() throws Exception {
        releaseHelper();
    }

}