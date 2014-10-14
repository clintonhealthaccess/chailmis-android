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
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;

public class ReceiveService {

    @Inject
    Context context;

    @Inject
    StockService stockService;
    @Inject
    AllocationService allocationService;
    @Inject
    AlertsService alertsService;

    @Inject
    CommoditySnapshotService commoditySnapshotService;

    @Inject
    DbUtil dbUtil;

    public List<String> getReadyAllocationIds() {
        return new ArrayList<>(Arrays.asList("UG-2004", "UG-2005"));
    }

    public List<String> getCompletedIds() {
        return new ArrayList<>();
    }

    public void saveReceive(Receive receive) {
        GenericDao<Receive> receiveDao = new GenericDao<>(Receive.class, context);
        receiveDao.create(receive);
        if (receive.getAllocation() != null) {
            Allocation allocation = receive.getAllocation();
            allocation.setReceived(true);
            allocationService.update(allocation);

            alertsService.deleteAllocationAlert(allocation);
        }
        saveReceiveItems(receive.getReceiveItems());
    }

    private void saveReceiveItems(List<ReceiveItem> receiveItems) {
        GenericDao<ReceiveItem> receiveItemDao = new GenericDao<>(ReceiveItem.class, context);
        for (ReceiveItem receiveItem : receiveItems) {
            receiveItemDao.create(receiveItem);
            stockService.increaseStockLevelFor(receiveItem.getCommodity(), receiveItem.getQuantityReceived());
            commoditySnapshotService.add(receiveItem);
        }
    }


    public int getTotalReceived(Commodity commodity, Date startingDate, Date endDate) {

        int totalReceived = 0;

        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            Dao<Receive, String> receiveDao = initialiseDao(openHelper, Receive.class);
            Dao<ReceiveItem, String> receiveItemDao = initialiseDao(openHelper, ReceiveItem.class);

            QueryBuilder<Receive, String> receiveQueryBuilder = receiveDao.queryBuilder();
            receiveQueryBuilder.where().between("created", startingDate, endDate);

            QueryBuilder<ReceiveItem, String> receiveItemQueryBuilder = receiveItemDao.queryBuilder();
            receiveItemQueryBuilder.where().eq("commodity_id", commodity.getId());
            receiveItemQueryBuilder.join(receiveQueryBuilder);

            List<ReceiveItem> receiveItems = receiveItemQueryBuilder.query();

            for(ReceiveItem receiveItem: receiveItems){
                totalReceived+=receiveItem.getQuantityReceived();
            }

        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
            return totalReceived;
        }
    }

    private <T> Dao<T, String> initialiseDao(SQLiteOpenHelper openHelper, Class<T> domainClass) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, domainClass);
    }
}
