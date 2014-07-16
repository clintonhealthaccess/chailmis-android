package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderService {
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
}
