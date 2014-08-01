package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DispensingService {

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

    public String getNextPrescriptionId() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
        String currentMonth = simpleDateFormat.format(new Date());
        int numberOfDispensingsToPatientsThisMonth = getDispensingsToPatientsThisMonth();
        return getFormattedPrescriptionId(currentMonth, numberOfDispensingsToPatientsThisMonth);
    }

    private String getFormattedPrescriptionId(String currentMonth, int numberOfDispensingsToPatientsThisMonth) {
        String stringOfZeros = "";

        int length = String.valueOf(numberOfDispensingsToPatientsThisMonth).length();
        if (length < 4) {
            for (int i = 0; i < 4 - length; i++) {
                stringOfZeros += "0";
            }
        }
        return String.format("%s%d-%s", stringOfZeros, numberOfDispensingsToPatientsThisMonth + 1, currentMonth);
    }

    private int getDispensingsToPatientsThisMonth() {
        return dbUtil.withDao(Dispensing.class, new DbUtil.Operation<Dispensing, Integer>() {
            @Override
            public Integer operate(Dao<Dispensing, String> dao) throws SQLException {
                QueryBuilder<Dispensing, String> dispensingStringQueryBuilder = dao.queryBuilder();
                Date firstDay = firstDayOfThisMonth();
                Date lastDay = lastDayOfThisMonth();
                dispensingStringQueryBuilder.where().between("created", firstDay, lastDay).and().eq("dispenseToFacility", false);
                PreparedQuery<Dispensing> query = dispensingStringQueryBuilder.prepare();
                List<Dispensing> dispensingList = dao.query(query);
                return dispensingList.size();
            }


        });
    }

    private Date lastDayOfThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, Calendar.getInstance().getActualMaximum(Calendar.MINUTE));
        return cal.getTime();
    }

    private Date firstDayOfThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, Calendar.getInstance().getActualMinimum(Calendar.MINUTE));
        return cal.getTime();
    }
}
