package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;

public class DispensingService {
    @Inject
    private Context context;

    @Inject
    private DbUtil dbUtil;
    @Inject
    StockService stockService;

    public void addDispensing(final Dispensing dispensing) {
        dbUtil.withDao(DispensingItem.class, new DbUtil.Operation<DispensingItem, DispensingItem>() {
            @Override
            public DispensingItem operate(Dao<DispensingItem, String> dao) throws SQLException {
                saveDispensing(dispensing);
                for (DispensingItem item : dispensing.getDispensingItems()) {
                    item.setDispensing(dispensing);
                    dao.create(item);
                    adjustStockLevel(item);
                }
                return null;
            }
        });
    }


    private void saveDispensing(final Dispensing dispensing) throws SQLException {
        dbUtil.withDao(Dispensing.class, new DbUtil.Operation<Dispensing, Dispensing>() {
            @Override
            public Dispensing operate(Dao<Dispensing, String> dao) throws SQLException {
                dao.create(dispensing);
                return dispensing;
            }
        });
    }

    private void adjustStockLevel(DispensingItem dispensing) throws SQLException {
        stockService.updateStockLevelFor(dispensing.getCommodity(), dispensing.getQuantity());
    }
}
