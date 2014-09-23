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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService implements OrderItemSaver {
    List<OrderReason> orderReasons;
    List<OrderType> orderTypes;
    @Inject
    private UserService userService;
    @Inject
    private LmisServer lmisServer;
    @Inject
    private DbUtil dbUtil;

    @Inject
    private CommoditySnapshotService commoditySnapshotService;

    @Inject
    private AlertsService alertsService;

    public List<OrderReason> syncOrderReasons() {
        final ArrayList<OrderReason> savedReasons = new ArrayList<>();
        final List<String> reasons = lmisServer.fetchOrderReasons(userService.getRegisteredUser());
        dbUtil.withDao(OrderReason.class, new DbUtil.Operation<OrderReason, Void>() {
            @Override
            public Void operate(Dao<OrderReason, String> dao) throws SQLException {
                saveReasons(dao, reasons, savedReasons);
                return null;
            }
        });

        return savedReasons;
    }

    private void saveReasons(Dao<OrderReason, String> dao, List<String> reasons, ArrayList<OrderReason> savedReasons) throws SQLException {
        dao.delete(dao.queryForAll());
        for (String reason : reasons) {
            OrderReason data = new OrderReason(reason);
            dao.create(data);
            savedReasons.add(data);
        }
    }

    public List<OrderType> allOrderTypes() {
        if (orderTypes == null) {
            orderTypes = dbUtil.withDao(OrderType.class, new DbUtil.Operation<OrderType, List<OrderType>>() {
                @Override
                public List<OrderType> operate(Dao<OrderType, String> dao) throws SQLException {
                    return dao.queryForAll();
                }
            });
        }
        return orderTypes;
    }

    public List<OrderReason> allOrderReasons() {
        if (orderReasons == null) {
            orderReasons = dbUtil.withDao(OrderReason.class, new DbUtil.Operation<OrderReason, List<OrderReason>>() {
                @Override
                public List<OrderReason> operate(Dao<OrderReason, String> dao) throws SQLException {
                    return dao.queryForAll();
                }
            });
        }
        return orderReasons;
    }

    public List<Order> all() {

        return dbUtil.withDao(Order.class, new DbUtil.Operation<Order, List<Order>>() {
            @Override
            public List<Order> operate(Dao<Order, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

    public String getNextSRVNumber() {
        String facilityCode = userService.getRegisteredUser().getFacilityCode();
        List<Order> orders = all();
        return getFormattedSRVNumber(facilityCode, orders.size());
    }

    private String getFormattedSRVNumber(String facilityCode, int numberOfOrders) {
        String stringOfZeros = "";

        int length = String.valueOf(numberOfOrders).length();
        if (length < 4) {
            for (int i = 0; i < 4 - length; i++) {
                stringOfZeros += "0";
            }
        }
        return String.format("%s-%s%d", facilityCode, stringOfZeros, numberOfOrders + 1);
    }

    public void saveOrder(final Order order) {
        dbUtil.withDao(Order.class, new DbUtil.Operation<Order, Void>() {
            @Override
            public Void operate(Dao<Order, String> dao) throws SQLException {
                dao.create(order);
                return null;
            }
        });

        order.saveOrderItems(this);

        if (!order.getOrderType().isRoutine()) {
            alertsService.disableAlertsForCommodities(getCommoditiesInOrder(order));
        }
    }

    private ImmutableList<Commodity> getCommoditiesInOrder(Order order) {
        return FluentIterable.from(order.getItems()).transform(new Function<OrderItem, Commodity>() {
            @Override
            public Commodity apply(OrderItem input) {
                return input.getCommodity();
            }
        }).toList();
    }

    @Override
    public void saveOrderItem(final OrderItem orderItem) {
        dbUtil.withDao(OrderItem.class, new DbUtil.Operation<OrderItem, String>() {
            @Override
            public String operate(Dao<OrderItem, String> dao) throws SQLException {
                dao.create(orderItem);
                commoditySnapshotService.add(orderItem);
                return null;
            }
        });
    }

    public void syncOrderTypes() {
        final List<OrderType> orderTypes = lmisServer.fetchOrderTypes(userService.getRegisteredUser());
        dbUtil.withDao(OrderType.class, new DbUtil.Operation<OrderType, Void>() {
            @Override
            public Void operate(Dao<OrderType, String> dao) throws SQLException {
                saveOrderTypes(dao, orderTypes);
                return null;
            }
        });

    }

    private void saveOrderTypes(Dao<OrderType, String> dao, List<OrderType> types) throws SQLException {
        dao.delete(dao.queryForAll());
        for (OrderType type : types) {
            dao.create(type);
        }
    }
}
