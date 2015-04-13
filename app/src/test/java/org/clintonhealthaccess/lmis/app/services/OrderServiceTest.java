/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.services;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshot;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.LMISTestCase;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.j256.ormlite.dao.DaoManager.createDao;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.addPendingHttpResponse;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderServiceTest extends LMISTestCase {

    public static final String SRV_NUMBER = "SRV NUMBER";
    public static final int QUANTITY = 10;
    @Inject
    private OrderService orderService;

    @Inject
    private DbUtil dbUtil;

    private UserService mockUserService;
    private LmisSqliteOpenHelper openHelper;
    private AndroidConnectionSource connectionSource;
    private String responseBody =
            "{\"optionSets\":[{\"id\":\"61a2525ca8d\",\"name\":\"Reasons For Order\",\"options\":[{\"name\":\"HIGH DEMAND\"},{\"name\":\"LOSSES\"},{\"name\":\"EXPIRIES\"}]}]}";
    private OrderReason highDemand = new OrderReason("HIGH DEMAND");
    private OrderReason losses = new OrderReason("LOSSES");
    private OrderReason expiries = new OrderReason("EXPIRIES");

    private Dao<OrderItem, String> orderItemDao;
    private Dao<OrderReason, ?> reasonDao;
    private Dao<OrderType, ?> orderTypeDao;
    private Dao<Order, String> orderDao;
    private Dao<Commodity, String> commodityDao;
    private Dao<CommodityAction, String> commodityActivityDao;
    private Dao<Category, ?> categoryDao;
    private Dao<DataSet, ?> dataSetDao;
    private GenericDao<CommoditySnapshot> snapshotGenericDao;
    private OrderType Routine = new OrderType(OrderType.ROUTINE);
    private OrderType Emergency = new OrderType(OrderType.EMERGENCY);

    @Before
    public void setUp() throws SQLException {
        LmisSqliteOpenHelper.getInstance(application).close();

        mockUserService = mock(UserService.class);
        when(mockUserService.getRegisteredUser()).thenReturn(new User("", "", "AU"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });

        openHelper = LmisSqliteOpenHelper.getInstance(application);// getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        reasonDao = createDao(connectionSource, OrderReason.class);
        orderDao = createDao(connectionSource, Order.class);
        orderItemDao = createDao(connectionSource, OrderItem.class);
        commodityDao = createDao(connectionSource, Commodity.class);
        commodityActivityDao = createDao(connectionSource, CommodityAction.class);
        categoryDao = createDao(connectionSource, Category.class);
        orderTypeDao = createDao(connectionSource, OrderType.class);
        dataSetDao = createDao(connectionSource, DataSet.class);
        snapshotGenericDao = new GenericDao<>(CommoditySnapshot.class, Robolectric.application);
    }

    @Test
    public void shouldGetReasonsForOrderFromDHIS2AndSaveThem() throws Exception {
        addPendingHttpResponse(200, responseBody);
        orderService.syncOrderReasons();
        List<OrderReason> orderReasons = reasonDao.queryForAll();
        assertThat(orderReasons, contains(highDemand, losses, expiries));
    }

    @Test
    public void shouldGetTypesOfOrderFromDHIS2AndSaveThem() throws Exception {
        setUpSuccessHttpGetRequest(200, "orderTypes.json");
        orderService.syncOrderTypes();
        List<OrderType> orderTypes = orderTypeDao.queryForAll();
        assertThat(orderTypes, contains(Routine, Emergency));
    }

    @Test
    public void shouldPersistAnOrder() throws SQLException {

        Order order = createOrder();

        orderService.saveOrder(order);

        Order returnedOrder = orderDao.queryForSameId(order);

        assertThat(orderDao.countOf(), is(1L));
        assertThat(returnedOrder.getSrvNumber(), is(order.getSrvNumber()));
        assertThat(orderItemDao.queryForAll().get(0).getQuantity(), is(QUANTITY));
    }

    @Test
    public void shouldGenerateSRVNumber() throws Exception {

        assertThat(orderService.getNextSRVNumber(), is("AU-0001"));
        OrderType type = new OrderType("routine");
        orderTypeDao.createOrUpdate(type);
        final Order order = new Order("AU-0001");
        order.setOrderType(type);
        dbUtil.withDao(application, Order.class, new DbUtil.Operation<Order, Order>() {
            @Override
            public Order operate(Dao<Order, String> dao) throws SQLException {
                dao.create(order);
                return order;
            }
        });
        assertThat(orderService.getNextSRVNumber(), is("AU-0002"));

    }

    @Test
    public void shouldGetAllOrderTypes() throws Exception {

        final OrderType routine = new OrderType("Routine");
        final OrderType emergency = new OrderType("emergency");

        dbUtil.withDao(OrderType.class, new DbUtil.Operation<OrderType, OrderType>() {
            @Override
            public OrderType operate(Dao<OrderType, String> dao) throws SQLException {
                dao.create(routine);
                dao.create(emergency);
                return null;
            }
        });

        assertThat(orderService.allOrderTypes().size(), is(2));
        assertThat(orderService.allOrderTypes(), contains(routine, emergency));

    }

    @Test
    public void shouldMakeSnapShotOfOrderItemWhenItIsSaved() throws Exception {
        Order order = createOrder();

        assertThat(snapshotGenericDao.countOf(), is(0L));
        orderService.saveOrder(order);

        Order returnedOrder = orderDao.queryForSameId(order);
        assertThat(orderDao.countOf(), is(1L));
        assertThat(returnedOrder.getSrvNumber(), is(order.getSrvNumber()));
        assertThat(snapshotGenericDao.countOf(), is(2L));
    }

    private Order createOrder() throws SQLException {
        Category category = new Category("Category");
        categoryDao.create(category);
        Commodity commodity = new Commodity("Commodity 1", category);
        commodityDao.create(commodity);
        DataSet dataSet = new DataSet("asdas");
        dataSetDao.create(dataSet);
        CommodityAction commodityAction = spy(new CommodityAction(commodity, "1212312312", "1212", OrderItem.ORDERED_AMOUNT));
        ArrayList<DataSet> dataSets = Lists.newArrayList(dataSet);
//        when(commodityAction.getDataSets()).thenReturn(dataSets);
        CommodityAction otherActivity = spy(new CommodityAction(commodity, "23323", "1212", OrderItem.ORDER_REASON));
//        when(otherActivity.getDataSets()).thenReturn(dataSets);

        commodityActivityDao.create(commodityAction);
        commodityActivityDao.create(otherActivity);

        commodity = commodityDao.queryForId(commodity.getId());

        OrderCommodityViewModel commodityViewModel = new OrderCommodityViewModel(commodity, QUANTITY);
        commodityViewModel.setOrderPeriodStartDate(new Date());
        commodityViewModel.setOrderPeriodEndDate(new Date());

        OrderReason emergency = new OrderReason("Emergency");
        reasonDao.create(emergency);

        OrderReason highDemand = new OrderReason("High demand");
        reasonDao.create(highDemand);
        commodityViewModel.setReasonForUnexpectedOrderQuantity(highDemand);
        OrderType type = new OrderType("routine");
        orderTypeDao.create(type);

        OrderItem orderItem = new OrderItem(commodityViewModel);
        Order order = new Order(SRV_NUMBER);
        order.setOrderType(type);
        order.addItem(orderItem);
        return order;
    }
}