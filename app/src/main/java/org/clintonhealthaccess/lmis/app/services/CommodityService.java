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
import android.content.SharedPreferences;
import android.util.TimingLogger;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import roboguice.inject.InjectResource;

import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class CommodityService {
    public static final String MONTHLY_STOCK_COUNT_DAY = "MONTHLY_STOCK_COUNT_DAY";
    public static final String ROUTINE_ORDER_ALERT_DAY = "ROUTINE_ORDER_ALERT_DAY";
    @Inject
    private LmisServer lmisServer;

    @Inject
    private CategoryService categoryService;

    @Inject
    private AllocationService allocationService;

    @Inject
    CommodityActionService commodityActionService;

    @Inject
    private DbUtil dbUtil;

    @Inject
    private Context context;

    @Inject
    SharedPreferences sharedPreferences;

    @InjectResource(R.string.monthly_stock_count_search_key)
    private String monthlyStockCountSearchKey;

    @InjectResource(R.string.routine_order_alert_day)
    private String routineOrderAlertDay;

    public void initialise(User user) {
        TimingLogger timingLogger = new TimingLogger("TIMER", "initialise");
        List<Category> allCommodities = lmisServer.fetchCommodities(user);
        timingLogger.addSplit("fetch all cats");
        saveToDatabase(allCommodities);
        timingLogger.addSplit("save all Cats");
        categoryService.clearCache();
        syncConstants(user);
        timingLogger.addSplit("sync constants");

        List<Commodity> commodities = all();
        timingLogger.addSplit("all");

        commodityActionService.syncCommodityActionValues(user, commodities);

        timingLogger.addSplit("actionValues");
        categoryService.clearCache();
        updateStockValues(all());
        timingLogger.addSplit("updateStockValues");
        allocationService.syncAllocations(user);
        timingLogger.addSplit("sync allocations");
        categoryService.clearCache();
        timingLogger.addSplit("clearCache");
        timingLogger.dumpToLog();
    }

    private void syncConstants(User user) {
        fetchAndSaveIntegerConstant(user, monthlyStockCountSearchKey, MONTHLY_STOCK_COUNT_DAY);
        fetchAndSaveIntegerConstant(user, routineOrderAlertDay, ROUTINE_ORDER_ALERT_DAY);
    }

    private void updateStockValues(List<Commodity> commodities) {
        List<StockItem> stockItems = FluentIterable.from(commodities).transform(new Function<Commodity, StockItem>() {
            @Override
            public StockItem apply(Commodity input) {
                CommodityAction commodityAction = input.getCommodityAction(CommodityAction.STOCK_ON_HAND);
                if (commodityAction != null && commodityAction.getActionLatestValue() != null) {
                    return new StockItem(input, Integer.parseInt(commodityAction.getActionLatestValue().getValue()));
                } else {
                    return new StockItem(input, 0);
                }
            }
        }).toList();

        for (StockItem item : stockItems) {
            createStock(item);
        }
    }

    private void fetchAndSaveIntegerConstant(User user, String stockCountSearchKey, String key) {
        Integer day = lmisServer.fetchIntegerConstant(user, stockCountSearchKey);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, day);
        editor.commit();
    }

    private void createStock(final StockItem item) {
        dbUtil.withDao(StockItem.class, new Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                dao.create(item);
                return null;
            }
        });
    }


    public List<Commodity> all() {
        List<Category> categories = categoryService.all();
        List<Commodity> commodities = new ArrayList<>();
        for (Category category : categories) {
            commodities.addAll(category.getCommodities());
        }
        return commodities;
    }

    public void saveToDatabase(final List<Category> allCommodities) {
        dbUtil.withDaoAsBatch(Category.class, new Operation<Category, Void>() {
            @Override
            public Void operate(Dao<Category, String> dao) throws SQLException {
                for (Category category : allCommodities) {
                    dao.createOrUpdate(category);
                    saveAllCommodities(category);
                }
                return null;
            }
        });
    }

    private void saveAllCommodities(final Category category) {
        dbUtil.withDaoAsBatch(Commodity.class, new Operation<Commodity, Void>() {
            @Override
            public Void operate(Dao<Commodity, String> dao) throws SQLException {
                for (Commodity commodity : category.getNotSavedCommodities()) {
                    commodity.setCategory(category);
                    dao.createOrUpdate(commodity);
                    createCommodityAction(commodity);
                }
                return null;
            }
        });
    }

    private void createCommodityAction(Commodity commodity) {
        GenericDao<CommodityAction> commodityActivityGenericDao = new GenericDao<>(CommodityAction.class, context);
        GenericDao<DataSet> dataSetGenericDao = new GenericDao<>(DataSet.class, context);
        final List<DataSet> dataSets = new ArrayList<>();
        final List<CommodityAction> actions = new ArrayList<>();
        for (CommodityAction commodityAction : commodity.getCommodityActions()) {
            if (commodityAction.getDataSet() != null) {
                dataSets.add(commodityAction.getDataSet());
            }
            if (commodityAction.getCommodity() == null) {
                commodityAction.setCommodity(commodity);
            }
            actions.add(commodityAction);
        }

        dataSetGenericDao.bulkOperation(new Operation<DataSet, Object>() {
            @Override
            public Object operate(Dao<DataSet, String> dao) throws SQLException {
                for (DataSet dataSet : dataSets) {
                    dao.createOrUpdate(dataSet);
                }
                return null;
            }
        });

        commodityActivityGenericDao.bulkOperation(new Operation<CommodityAction, Object>() {
            @Override
            public Object operate(Dao<CommodityAction, String> dao) throws SQLException {
                for (CommodityAction action : actions) {
                    dao.createOrUpdate(action);
                }
                return null;
            }
        });

    }

    public List<Commodity> getMost5HighlyConsumedCommodities() {

        List<Commodity> commodities = all();
        Collections.sort(commodities, new Comparator<Commodity>() {
            @Override
            public int compare(Commodity lhs, Commodity rhs) {
                return rhs.getAMC().compareTo(lhs.getAMC());
            }
        });
        if (commodities.size() > 5) {
            return commodities.subList(0, 5);
        } else {
            return commodities;
        }
    }

}
