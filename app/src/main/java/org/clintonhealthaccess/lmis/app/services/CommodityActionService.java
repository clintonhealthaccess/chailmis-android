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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.AdjustmentsViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static java.lang.StrictMath.abs;

public class CommodityActionService {

    @Inject
    Context context;

    @Inject
    DbUtil dbUtil;

    @Inject
    private LmisServer lmisServer;

    public CommodityActionService() {
    }

    public List<CommodityAction> getAllById(final List<String> ids) {
        return dbUtil.withDao(CommodityAction.class, new DbUtil.Operation<CommodityAction, List<CommodityAction>>() {
            @Override
            public List<CommodityAction> operate(Dao<CommodityAction, String> dao) throws SQLException {
                return dao.queryBuilder().where().in("id", ids).query();
            }
        });
    }

    public List<CommodityAction> getAllocationId() {
        return dbUtil.withDao(CommodityAction.class, new DbUtil.Operation<CommodityAction, List<CommodityAction>>() {
            @Override
            public List<CommodityAction> operate(Dao<CommodityAction, String> dao) throws SQLException {
                return dao.queryBuilder().where().in("name", Arrays.asList(DataElementType.ALLOCATION_ID.getActivity())).query();
            }
        });
    }

    public CommodityAction save(CommodityAction commodityAction) {
        GenericDao<CommodityAction> commodityActivityDao = new GenericDao<>(CommodityAction.class, context);
        return commodityActivityDao.create(commodityAction);
    }

    protected void saveActionValues(final List<CommodityActionValue> commodityActionValues) {
        if (commodityActionValues != null) {
            dbUtil.withDaoAsBatch(CommodityActionValue.class, new DbUtil.Operation<CommodityActionValue, Void>() {
                        @Override
                        public Void operate(Dao<CommodityActionValue, String> dao) throws SQLException {
                            for (CommodityActionValue actionValue : commodityActionValues) {
                                dao.createOrUpdate(actionValue);
                            }
                            return null;
                        }
                    }
            );
        }
    }

    public void syncCommodityActionValues(User user) {
        List<CommodityActionValue> commodityActionValues = lmisServer.fetchCommodityActionValues(user);
        saveActionValues(commodityActionValues);
    }

    public void syncIndicatorValues(User user, List<Commodity> commodities) {
        List<CommodityActionValue> commodityActionValues = lmisServer.fetchIndicatorValues(user, commodities);
        saveActionValues(commodityActionValues);
    }

    public int getMonthlyValue(Commodity commodity, Date startingDate, Date endDate, DataElementType dataElementType) {
        SQLiteOpenHelper openHelper = LmisSqliteOpenHelper.getInstance(context);
        int commodityActionValueQuantity = 0;
        try {
            Dao<CommodityActionValue, String> commodityActionValueDao = DbUtil.initialiseDao(openHelper, CommodityActionValue.class);
            Dao<CommodityAction, String> commodityActionDao = DbUtil.initialiseDao(openHelper, CommodityAction.class);

            QueryBuilder<CommodityActionValue, String> commodityActionValueQueryBuilder = commodityActionValueDao.queryBuilder();
            List<String> periods = monthlyPeriods(startingDate, endDate);

            commodityActionValueQueryBuilder.where().in("period", periods);

            QueryBuilder<CommodityAction, String> commodityActionQueryBuilder = commodityActionDao.queryBuilder();
            commodityActionQueryBuilder.where().eq("commodity_id", commodity.getId()).and().eq("activityType", dataElementType);

            commodityActionValueQueryBuilder.join(commodityActionQueryBuilder);

            List<CommodityActionValue> commodityActionValues = commodityActionValueQueryBuilder.query();
            commodityActionValueQuantity = average(commodityActionValues, periods.size());


        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
            return commodityActionValueQuantity;
        }
    }

    private List<String> monthlyPeriods(Date startDate, Date endDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = calendar.getTime();

        List<String> periods = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");

        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate)) {
            periods.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.MONTH, 1);
        }

        return periods;
    }

    private int average(List<CommodityActionValue> commodityActionValues, int maxNumberOfValues) {
        int value = 0;
        for (CommodityActionValue commodityActionValue : commodityActionValues) {
            value += Integer.parseInt(commodityActionValue.getValue());
        }
        return value / maxNumberOfValues;
    }
}
