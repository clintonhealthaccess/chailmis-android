package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
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
import java.util.Date;
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
    private String responseBody = "{\"order_reasons\":[\"Emergency\",\"Routine\"],\"unexpected_quantity_reasons\":[\"High Demand\",\"Losses\"]}";

    private OrderReason emergency = new OrderReason("Emergency", OrderReason.ORDER_REASONS_JSON_KEY);
    private OrderReason routine = new OrderReason("Routine", OrderReason.ORDER_REASONS_JSON_KEY);
    private OrderReason highDemand = new OrderReason("High Demand", OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);
    private OrderReason losses = new OrderReason("Losses", OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);

    private Dao<OrderItem, String> orderItemDao;
    private Dao<OrderReason, ?> reasonDao;
    private Dao<Order, String> orderDao;
    private Dao<Commodity, String> commodityDao;
    private Dao<Category, ?> categoryDao;

    @Before
    public void setUp() throws SQLException {
        mockUserService = mock(UserService.class);
        when(mockUserService.getRegisteredUser()).thenReturn(new User("", "", "AU"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });

        openHelper = getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        reasonDao = createDao(connectionSource, OrderReason.class);
        orderDao = createDao(connectionSource, Order.class);
        orderItemDao = createDao(connectionSource, OrderItem.class);
        commodityDao = createDao(connectionSource, Commodity.class);
        categoryDao = createDao(connectionSource, Category.class);
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

    @Test
    public void shouldPersistAnOrder() throws SQLException {

        Category category = new Category("Category");
        categoryDao.create(category);

        Commodity commodity = new Commodity("Commodity 1", category);
        commodityDao.create(commodity);

        CommodityViewModel commodityViewModel = new CommodityViewModel(commodity, 10);
        commodityViewModel.setOrderPeriodStartDate(new Date());
        commodityViewModel.setOrderPeriodEndDate(new Date());

        OrderReason emergency = new OrderReason("Emergency", OrderReason.ORDER_REASONS_JSON_KEY);
        reasonDao.create(emergency);
        commodityViewModel.setReasonForOrder(emergency);

        OrderReason highDemand = new OrderReason("High demand", OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);
        reasonDao.create(highDemand);
        commodityViewModel.setReasonForUnexpectedOrderQuantity(highDemand);

        OrderItem orderItem = new OrderItem(commodityViewModel);
        Order order = new Order();
        order.addItem(orderItem);

        orderService.saveOrder(order);

        Order returnedOrder = orderDao.queryForSameId(order);
        assertThat(orderDao.countOf(), is(1L));
        assertThat(returnedOrder.getSrvNumber(), is(order.getSrvNumber()));

        assertThat(orderItemDao.queryForAll().get(0), is(orderItem));
    }

    @Test
    public void shouldGenerateSRVNumber() throws Exception {

        assertThat(orderService.getNextSRVNumber(), is("AU-0001"));
        final Order order = new Order("AU-0001");
        dbUtil.withDao(Order.class, new DbUtil.Operation<Order, Order>() {
            @Override
            public Order operate(Dao<Order, String> dao) throws SQLException {
                dao.create(order);
                return order;
            }
        });
        assertThat(orderService.getNextSRVNumber(), is("AU-0002"));

    }

    @After
    public void tearDown() throws Exception {
        releaseHelper();
    }

}