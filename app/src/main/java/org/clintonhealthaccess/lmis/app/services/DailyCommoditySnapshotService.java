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

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.models.DailyCommoditySnapshot;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailyCommoditySnapshotService {

    public static final String COMMODITY_ID = "commodity_id";
    public static final String COMMODITY_ACTIVITY_ID = "commodityActivity_id";
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    public void add(final Snapshotable snapshotable) {
        GenericDao<DailyCommoditySnapshot> dailyCommoditySnapshotDao = new GenericDao<DailyCommoditySnapshot>(DailyCommoditySnapshot.class, context);

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = getSnapshotsForCommodityToday(snapshotable);

        if (dailyCommoditySnapshots.isEmpty()) {
            createNewSnaphot(snapshotable, dailyCommoditySnapshotDao);
        } else {
            updateSnapshot(snapshotable, dailyCommoditySnapshotDao, dailyCommoditySnapshots);
        }
    }

    private void updateSnapshot(Snapshotable snapshotable, GenericDao<DailyCommoditySnapshot> dailyCommoditySnapshotDao, List<DailyCommoditySnapshot> dailyCommoditySnapshots) {
        DailyCommoditySnapshot commoditySnapshot = dailyCommoditySnapshots.get(0);
        commoditySnapshot.incrementValue(snapshotable.getValue());
        commoditySnapshot.setSynced(false);
        dailyCommoditySnapshotDao.update(commoditySnapshot);
    }

    private void createNewSnaphot(Snapshotable snapshotable, GenericDao<DailyCommoditySnapshot> dailyCommoditySnapshotDao) {
        DailyCommoditySnapshot commoditySnapshot = new DailyCommoditySnapshot(snapshotable.getCommodity(), snapshotable.getActivity(), snapshotable.getValue());
        dailyCommoditySnapshotDao.create(commoditySnapshot);
    }

    private List<DailyCommoditySnapshot> getSnapshotsForCommodityToday(final Snapshotable snapshotable) {
        return dbUtil.withDao(DailyCommoditySnapshot.class, new DbUtil.Operation<DailyCommoditySnapshot, List<DailyCommoditySnapshot>>() {
            @Override
            public List<DailyCommoditySnapshot> operate(Dao<DailyCommoditySnapshot, String> dao) throws SQLException {
                QueryBuilder<DailyCommoditySnapshot, String> queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq(COMMODITY_ID, snapshotable.getCommodity()).and().eq(COMMODITY_ACTIVITY_ID, snapshotable.getActivity()).and().between("date", startOfDay(), endOfDay());
                return dao.query(queryBuilder.prepare());
            }
        });
    }

    public List<DailyCommoditySnapshot> getUnSyncedSnapshots() {
        return dbUtil.withDao(DailyCommoditySnapshot.class, new DbUtil.Operation<DailyCommoditySnapshot, List<DailyCommoditySnapshot>>() {
            @Override
            public List<DailyCommoditySnapshot> operate(Dao<DailyCommoditySnapshot, String> dao) throws SQLException {
                QueryBuilder<DailyCommoditySnapshot, String> queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq("synced", false);
                return dao.query(queryBuilder.prepare());
            }
        });
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
