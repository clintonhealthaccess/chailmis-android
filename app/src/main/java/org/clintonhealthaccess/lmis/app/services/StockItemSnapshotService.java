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
import android.util.Log;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StockItemSnapshotService {

    @Inject
    DbUtil dbUtil;
    @Inject
    private Context context;

    public StockItemSnapshot get(final Commodity commodity, final Date date) throws Exception {
        List<StockItemSnapshot> stockItemSnapshots = dbUtil.withDao(StockItemSnapshot.class,
                new DbUtil.Operation<StockItemSnapshot, List<StockItemSnapshot>>() {
                    @Override
                    public List<StockItemSnapshot> operate(Dao<StockItemSnapshot, String> dao) throws SQLException {
                        QueryBuilder<StockItemSnapshot, String> queryBuilder = dao.queryBuilder();
                        queryBuilder.where().eq("commodity_id", commodity.getId()).and().eq("created", date);
                        PreparedQuery<StockItemSnapshot> query = queryBuilder.prepare();

                        return dao.query(query);
                    }
                }
        );

        if (stockItemSnapshots.size() > 1) {
            throw new Exception("Multiple stock item snapshots found for " + commodity.getName() +
                    " on " + new SimpleDateFormat("yyyy-MM-dd").format(date));
        }

        if (stockItemSnapshots.size() == 1) {
            return stockItemSnapshots.get(0);
        }

        return null;
    }

    public List<StockItemSnapshot> get(final Commodity commodity, final Date startDate, final Date endDate) {
        return dbUtil.withDao(StockItemSnapshot.class,
                new DbUtil.Operation<StockItemSnapshot, List<StockItemSnapshot>>() {
                    @Override
                    public List<StockItemSnapshot> operate(Dao<StockItemSnapshot, String> dao) throws SQLException {
                        QueryBuilder<StockItemSnapshot, String> queryBuilder = dao.queryBuilder();

                        queryBuilder.where().eq("commodity_id", commodity.getId())
                                .and().ge("created", startDate).and().le("created", endDate);
                        return queryBuilder.query();
                    }
                }
        );
    }

    public void createOrUpdate(Commodity commodity, Date date) {

        try {
            StockItemSnapshot stockItemSnapshot = get(commodity, date);
            if (stockItemSnapshot == null) {
                System.out.println("No Snapshot found--creating");
                create(commodity, date);
            } else {
                System.out.println("snapshot found--"+stockItemSnapshot.getQuantity());
                stockItemSnapshot.setQuantity(commodity.getStockOnHand());
                System.out.println("changed it to --"+commodity.getStockOnHand());
                update(stockItemSnapshot);
            }

        } catch (Exception e) {
            Log.e("StockItemSnapshot", e.getMessage());
        }
    }

    private void update(StockItemSnapshot stockItemSnapshot) {
        new GenericDao<>(StockItemSnapshot.class, context).update(stockItemSnapshot);
    }

    private void create(Commodity commodity, Date date) {
        StockItemSnapshot stockItemSnapshot = new StockItemSnapshot(commodity, date, commodity.getStockOnHand());
        new GenericDao<>(StockItemSnapshot.class, context).create(stockItemSnapshot);
    }

    public int getLatestStock(Commodity commodity, Date date, boolean isOpeningStock) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (isOpeningStock) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Date requiredDate = calendar.getTime();

        StockItemSnapshot latestStockItemSnapshot = getLatest(commodity, requiredDate);
        if (latestStockItemSnapshot != null) {
            return latestStockItemSnapshot.getQuantity();
        }

        return 0;
    }

    public int getLatestStock(Commodity commodity, Date date, boolean isOpeningStock, List<StockItemSnapshot> stockItemSnapshots) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (isOpeningStock) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Date requiredDate = calendar.getTime();

        StockItemSnapshot latestStockItemSnapshot = getLatest(commodity,
                requiredDate);
        if (latestStockItemSnapshot != null) {
            return latestStockItemSnapshot.getQuantity();
        }

        return 0;
    }

    public StockItemSnapshot getLatest(final Commodity commodity, final Date currentDate) {
        return dbUtil.withDao(StockItemSnapshot.class,
                new DbUtil.Operation<StockItemSnapshot, StockItemSnapshot>() {
                    @Override
                    public StockItemSnapshot operate(Dao<StockItemSnapshot, String> dao) throws SQLException {
                        QueryBuilder<StockItemSnapshot, String> queryBuilder = dao.queryBuilder();
                        queryBuilder.where().eq("commodity_id", commodity.getId()).and()
                                .le("created", currentDate);
                        queryBuilder.orderBy("created", false);
                        PreparedQuery<StockItemSnapshot> query = queryBuilder.prepare();

                        return dao.queryForFirst(query);
                    }
                }
        );

    }

    public StockItemSnapshot getSnapshot(final Date date, List<StockItemSnapshot> stockItemSnapshots) {
        for (StockItemSnapshot snapshot : stockItemSnapshots) {
            if (DateUtil.equal(snapshot.getCreated(), date)) {
                return snapshot;
            }
        }
        return null;
    }

    public int getStockOutDays(Commodity commodity, Date startingDate, Date endDate) throws Exception {

        int openingStock = getLatestStock(commodity, startingDate, true);

        int numOfStockOutDays = 0;

        List<StockItemSnapshot> snapshots = get(commodity, startingDate, endDate);

        Date closingDate = DateUtil.addDayOfMonth(endDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startingDate);

        while (calendar.getTime().before(closingDate)) {

            StockItemSnapshot stockItemSnapshot = getSnapshot(calendar.getTime(), snapshots);

            if (stockItemSnapshot != null && stockItemSnapshot.isStockOut() || stockItemSnapshot == null && openingStock == 0) {
                numOfStockOutDays++;
            }

            if(stockItemSnapshot!=null) {
                openingStock = stockItemSnapshot.getQuantity();
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return numOfStockOutDays;
    }

    public boolean isStockOutDay(Date date, Commodity commodity) {
        StockItemSnapshot latestSnapshot = getLatest(commodity, date);
        if (latestSnapshot != null) {
            if (DateUtil.equal(latestSnapshot.getCreated(), date)) {
                return latestSnapshot.isStockOut();
            } else {
                if (latestSnapshot.getQuantity() == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
