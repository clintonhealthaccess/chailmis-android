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
import java.util.HashMap;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
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

    private String responseBody = "{\"order_reasons\":[\"Emergency\",\"Routine\"],\"unexpected_quantity_reasons\":[\"High Demand\",\"Losses\"]}";
    private OrderReason emergency = new OrderReason("Emergency", OrderReason.ORDER_REASONS_JSON_KEY);
    private OrderReason routine = new OrderReason("Routine", OrderReason.ORDER_REASONS_JSON_KEY);
    private OrderReason highDemand = new OrderReason("High Demand", OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);
    private OrderReason losses = new OrderReason("Losses", OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);

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
        addPendingHttpResponse(200, responseBody);

        orderService.syncReasons();

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("type", OrderReason.ORDER_REASONS_JSON_KEY);
        List<OrderReason> orderReasons = reasonDao.queryForFieldValues(queryParams);

        assertThat(orderReasons, contains(emergency, routine));
    }

    @Test
    public void shouldGetReasonsForUnexpectedOrderQuantitiesFromDHIS2AndSaveThem() throws Exception {
        addPendingHttpResponse(200, responseBody);

        orderService.syncReasons();

        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("type", OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);
        List<OrderReason> orderReasons = reasonDao.queryForFieldValues(queryParams);

        assertThat(orderReasons, contains(highDemand, losses));
    }

    @Test
    public void shouldReplaceAllReasonsInDatabaseAfterFetch() throws Exception {
        addPendingHttpResponse(200, responseBody);
        orderService.syncReasons();
        assertThat(reasonDao.countOf(), is(4L));

        addPendingHttpResponse(200, responseBody);
        orderService.syncReasons();
        assertThat(reasonDao.countOf(), is(4L));
    }

    @Test
    public void shouldProvideAllReasonsForOrders() {
        addPendingHttpResponse(200, responseBody);
        orderService.syncReasons();

        List<OrderReason> reasons = orderService.allOrderReasons();
        assertThat(reasons, containsInAnyOrder(emergency, routine, losses, highDemand));
    }

    @After
    public void tearDown() throws Exception {
        releaseHelper();
    }

}