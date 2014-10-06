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

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshot;
import org.clintonhealthaccess.lmis.app.models.CommoditySnapshotValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSetPushResponse;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.sms.SmsSyncService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.e;
import static android.util.Log.i;
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
    SmsSyncService smsSyncService;

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

    public void syncWithServer(User user) {
        List<CommoditySnapshot> snapshotsToSync = getUnSyncedSnapshots();
        if (!isEmpty(snapshotsToSync)) {
            i("==> Syncing...........", snapshotsToSync.size() + " snapshots");
            DataValueSet valueSet = getDataValueSetFromSnapshots(snapshotsToSync, user.getFacilityCode());
            try {
                DataValueSetPushResponse response = lmisServer.pushDataValueSet(valueSet, user);
                if (response.isSuccess()) {
                    markSnapShotsAsSynced(snapshotsToSync);
                } else {
                    syncThroughSms(snapshotsToSync, user);
                }
            } catch (LmisException ex) {
                e("==> Syncing...........", snapshotsToSync.size() + " snapshots failed");
                syncThroughSms(snapshotsToSync, user);
            }
        }
    }

    private void syncThroughSms(List<CommoditySnapshot> snapshotsToSync, User user) {
        DataValueSet valueSet = getDataValueSetFromSnapshots(snapshotsToSync, user.getFacilityCode());
        boolean sentBySms = smsSyncService.send(valueSet);
        if(sentBySms) {
            markSnapShotsAsSynced(snapshotsToSync);
        }
    }

    private void markSnapShotsAsSynced(final List<CommoditySnapshot> snapshotsToSync) {
        GenericDao<CommoditySnapshot> dailyCommoditySnapshotDao = new GenericDao<>(CommoditySnapshot.class, context);
        for (CommoditySnapshot snapshot : snapshotsToSync) {
            snapshot.setSynced(true);
            dailyCommoditySnapshotDao.update(snapshot);
        }
    }

    protected DataValueSet getDataValueSetFromSnapshots(List<CommoditySnapshot> snapshotsToSync, String orgUnit) {
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataSet(snapshotsToSync.get(0).getCommodityAction().getDataSet().getId());
        dataValueSet.setDataValues(new ArrayList<DataValue>());
        for (CommoditySnapshot snapshot : snapshotsToSync) {
            DataValue dataValue = DataValue.builder().value(String.valueOf(snapshot.getValue())).
                    dataElement(snapshot.getCommodityAction().getId()).
                    period(snapshot.getPeriod()).orgUnit(orgUnit).attributeOptionCombo(snapshot.getAttributeOptionCombo()).build();
            dataValueSet.getDataValues().add(dataValue);
        }
        return dataValueSet;
    }
}
