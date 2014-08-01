package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.models.DailyCommoditySnapshot;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailyCommoditySnapshotService {

    public static final String COMMODITY_ID = "commodity_id";
    public static final String AGGREGATION_FIELD_ID = "aggregationField_id";
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    public void add(final Snapshotable snapshotable) {
        GenericDao<DailyCommoditySnapshot> dailyCommoditySnapshotDao = new GenericDao<DailyCommoditySnapshot>(DailyCommoditySnapshot.class, context);

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = dbUtil.withDao(DailyCommoditySnapshot.class, new DbUtil.Operation<DailyCommoditySnapshot, List<DailyCommoditySnapshot>>() {
            @Override
            public List<DailyCommoditySnapshot> operate(Dao<DailyCommoditySnapshot, String> dao) throws SQLException {
                QueryBuilder<DailyCommoditySnapshot, String> queryBuilder = dao.queryBuilder();
                Date startOfDay = startOfDay();
                Date endOfDay = endOfDay();
                queryBuilder.where().eq(COMMODITY_ID, snapshotable.getCommodity()).and().eq(AGGREGATION_FIELD_ID, snapshotable.getAggregationField()).and().between("date", startOfDay, endOfDay);
                PreparedQuery<DailyCommoditySnapshot> query = queryBuilder.prepare();
                return dao.query(query);
            }
        });

        if (dailyCommoditySnapshots.size() > 0) {
            DailyCommoditySnapshot commoditySnapshot = dailyCommoditySnapshots.get(0);
            commoditySnapshot.incrementValue(snapshotable.getValue());
            commoditySnapshot.setSynced(false);
            dailyCommoditySnapshotDao.update(commoditySnapshot);
        } else {
            DailyCommoditySnapshot commoditySnapshot = new DailyCommoditySnapshot(snapshotable.getCommodity(), snapshotable.getAggregationField(), snapshotable.getValue());
            dailyCommoditySnapshotDao.create(commoditySnapshot);
        }


    }

    private Date startOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, Calendar.getInstance().getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, Calendar.getInstance().getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, Calendar.getInstance().getActualMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    private Date endOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, Calendar.getInstance().getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, Calendar.getInstance().getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, Calendar.getInstance().getActualMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }
}
