package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.interceptors.AuthInterceptor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

public class OrderService {
    @Inject
    private Context context;

    @Inject
    private UserService userService;

    @Inject
    private DbUtil dbUtil;

    private List<String> getReasons() {
        AuthInterceptor requestInterceptor = new AuthInterceptor(userService.getRegisteredUser());
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(requestInterceptor)
                .setEndpoint(context.getString(R.string.dhis2_base_url))
                .setClient(new ApacheClient())
                .build();

        DHIS2ApiService service = restAdapter.create(DHIS2ApiService.class);
        return service.getReasonsForOrder();
    }

    public List<OrderReason> syncReasons() {
        final ArrayList<OrderReason> savedReasons = new ArrayList<>();
        final List<String> reasons = getReasons();
        dbUtil.withDao(OrderReason.class, new DbUtil.Operation<OrderReason, Void>() {
            @Override
            public Void operate(Dao<OrderReason, String> dao) throws SQLException {
                dao.delete(dao.queryForAll());
                for (String reason : reasons) {
                    OrderReason data = new OrderReason(reason);
                    dao.create(data);
                    savedReasons.add(data);
                }
                return null;
            }
        });
        return savedReasons;
    }
}
