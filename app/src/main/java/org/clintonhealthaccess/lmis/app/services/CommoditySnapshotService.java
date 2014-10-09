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

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshot;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshotValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSetPushResponse;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.sms.SmsSyncService;

import java.sql.SQLException;
import java.util.List;

import static android.util.Log.e;
import static android.util.Log.i;
import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.clintonhealthaccess.lmis.app.models.CommoditySnapshot.PERIOD;
import static org.clintonhealthaccess.lmis.app.utils.Helpers.isEmpty;

public class CommoditySnapshotService {

    public static final String COMMODITY_ID = "commodity_id";
    public static final String COMMODITY_ACTIVITY_ID = "commodityActivity_id";
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    @Inject
    private LmisServer lmisServer;

    @Inject
    private SmsSyncService smsSyncService;

    public void add(final Snapshotable snapshotable) {
        GenericDao<CommoditySnapshot> snapshotGenericDao = new GenericDao<CommoditySnapshot>(CommoditySnapshot.class, context);
        for (CommoditySnapshotValue value : snapshotable.getActivitiesValues()) {
            List<CommoditySnapshot> commoditySnapshots = getSnapshotsForCommodityPeriod(value);
            if (commoditySnapshots.isEmpty()) {
                createNewSnapshot(value, snapshotGenericDao);
            } else {
                updateSnapshot(value, snapshotGenericDao, commoditySnapshots);
            }
        }

    }

    private void updateSnapshot(CommoditySnapshotValue commoditySnapshotValue, GenericDao<CommoditySnapshot> dailyCommoditySnapshotDao, List<CommoditySnapshot> commoditySnapshots) {
        CommoditySnapshot commoditySnapshot = commoditySnapshots.get(0);
        commoditySnapshot.incrementValue(commoditySnapshotValue.getValue());
        commoditySnapshot.setSynced(false);
        dailyCommoditySnapshotDao.update(commoditySnapshot);
    }

    private void createNewSnapshot(CommoditySnapshotValue commoditySnapshotValue, GenericDao<CommoditySnapshot> dailyCommoditySnapshotDao) {
        CommoditySnapshot commoditySnapshot = new CommoditySnapshot(commoditySnapshotValue);
        dailyCommoditySnapshotDao.create(commoditySnapshot);
    }

    private List<CommoditySnapshot> getSnapshotsForCommodityPeriod(final CommoditySnapshotValue commoditySnapshotValue) {
        return dbUtil.withDao(CommoditySnapshot.class, new DbUtil.Operation<CommoditySnapshot, List<CommoditySnapshot>>() {
            @Override
            public List<CommoditySnapshot> operate(Dao<CommoditySnapshot, String> dao) throws SQLException {
                QueryBuilder<CommoditySnapshot, String> queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq(COMMODITY_ID, commoditySnapshotValue.getCommodityAction().getCommodity()).and().eq(COMMODITY_ACTIVITY_ID, commoditySnapshotValue.getCommodityAction()).and().eq(PERIOD, commoditySnapshotValue.getCommodityAction().getPeriod());
                return dao.query(queryBuilder.prepare());
            }
        });
    }

    public List<CommoditySnapshot> getUnSyncedSnapshots() {
        return dbUtil.withDao(CommoditySnapshot.class, new DbUtil.Operation<CommoditySnapshot, List<CommoditySnapshot>>() {
            @Override
            public List<CommoditySnapshot> operate(Dao<CommoditySnapshot, String> dao) throws SQLException {
                QueryBuilder<CommoditySnapshot, String> queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq("synced", false);
                return dao.query(queryBuilder.prepare());
            }
        });
    }

    public List<CommoditySnapshot> getSmsReadySnapshots() {
        List<CommoditySnapshot> unSyncedSnapshots = getUnSyncedSnapshots();
        return from(unSyncedSnapshots).filter(new Predicate<CommoditySnapshot>() {
            @Override
            public boolean apply(CommoditySnapshot input) {
                return !input.isSmsSent();
            }
        }).toList();
    }

    public void syncWithServer(User user) {
        List<CommoditySnapshot> snapshotsToSync = getUnSyncedSnapshots();
        if (!isEmpty(snapshotsToSync)) {
            i("==> Syncing...........", snapshotsToSync.size() + " snapshots");
            DataValueSet valueSet = new DataValueSet(snapshotsToSync, user.getFacilityCode());
            try {
                DataValueSetPushResponse response = lmisServer.pushDataValueSet(valueSet, user);
                if (response.isSuccess()) {
                    markSnapShotsAsSynced(snapshotsToSync);
                }
            } catch (LmisException ex) {
                e("==> Syncing...........", snapshotsToSync.size() + " snapshots failed");
            }
        }
    }

    public void syncWithServerThroughSms(User user) {
        i("SMS Sync", "Checking snapshots...");
        List<CommoditySnapshot> snapshots = getSmsReadySnapshots();
        i("SMS Sync", format("%d snapshots need to be synced through SMS", snapshots.size()));
        if(!isEmpty(snapshots)) {
            DataValueSet valueSet = new DataValueSet(snapshots, user.getFacilityCode());
            if(smsSyncService.send(valueSet)) {
                markSnapShotsAsSmsSent(snapshots);
            }
        }
    }

    private void markSnapShotsAsSmsSent(List<CommoditySnapshot> snapshots) {
        GenericDao<CommoditySnapshot> dao = new GenericDao<>(CommoditySnapshot.class, context);
        for (CommoditySnapshot snapshot : snapshots) {
            snapshot.setSmsSent(true);
            dao.update(snapshot);
        }
    }

    private void markSnapShotsAsSynced(final List<CommoditySnapshot> snapshotsToSync) {
        GenericDao<CommoditySnapshot> dailyCommoditySnapshotDao = new GenericDao<>(CommoditySnapshot.class, context);
        for (CommoditySnapshot snapshot : snapshotsToSync) {
            snapshot.setSynced(true);
            dailyCommoditySnapshotDao.update(snapshot);
        }
    }
}
