package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderService implements OrderItemSaver {
    List<OrderReason> orderReasons;
    @Inject
    private UserService userService;
    @Inject
    private LmisServer lmisServer;
    @Inject
    private DbUtil dbUtil;

    public List<OrderReason> syncReasons() {
        final ArrayList<OrderReason> savedReasons = new ArrayList<>();
        final Map<String, List<String>> reasons = lmisServer.fetchOrderReasons(userService.getRegisteredUser());

        dbUtil.withDao(OrderReason.class, new DbUtil.Operation<OrderReason, Void>() {
            @Override
            public Void operate(Dao<OrderReason, String> dao) throws SQLException {
                dao.delete(dao.queryForAll());
                for (String key : reasons.keySet()) {
                    for (String reason : reasons.get(key)) {
                        OrderReason data = new OrderReason(reason, key);
                        dao.create(data);
                        savedReasons.add(data);
                    }
                }

                return null;
            }
        });

        return savedReasons;
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
    }

    @Override
    public void saveOrderItem(final OrderItem orderItem) {
        dbUtil.withDao(OrderItem.class, new DbUtil.Operation<OrderItem, String>() {
            @Override
            public String operate(Dao<OrderItem, String> dao) throws SQLException {
                dao.create(orderItem);
                return null;
            }
        });
    }
}
