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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.Dhis2;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.e;
import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class CommodityService {
    public static final String MONTHLY_STOCK_COUNT_DAY = "MONTHLY_STOCK_COUNT_DAY";
    @Inject
    private LmisServer lmisServer;

    @Inject
    private CategoryService categoryService;

    @Inject
    private AllocationService allocationService;

    @Inject
    private DbUtil dbUtil;

    @Inject
    private Context context;

    @Inject
    SharedPreferences sharedPreferences;

    public void initialise(User user) {
        List<Category> allCommodities = lmisServer.fetchCommodities(user);
        saveToDatabase(allCommodities);
        categoryService.clearCache();
        fetchAndSaveMonthlyStockCountDay(user);

        List<Commodity> commodities = all();
        List<CommodityActionValue> commodityActionValues = lmisServer.fetchCommodityActionValues(commodities, user);
        saveActionValues(commodityActionValues);
        updateStockValues();

        //FIXME: https://github.com/chailmis/chailmis-android/issues/36
        allocationService.syncAllocations();
        categoryService.clearCache();
    }

    private void updateStockValues() {
        List<Commodity> commodities = all();
        List<StockItem> stockItems = FluentIterable.from(commodities).transform(new Function<Commodity, StockItem>() {
            @Override
            public StockItem apply(Commodity input) {
                CommodityAction commodityAction = input.getCommodityAction(CommodityAction.stockOnHand);
                if (commodityAction != null) {
                    System.out.println("action " + commodityAction.getActivityType());
                    System.out.println("action " + commodityAction.getName());
                    System.out.println("action " + commodityAction.getActionLatestValue());
                    System.out.println("action " + commodityAction.getCommodityActionValueList().size());
                }

                if (commodityAction != null && commodityAction.getActionLatestValue() != null) {
                    System.out.println("some value " + input.getName());
                    return new StockItem(input, Integer.parseInt(commodityAction.getActionLatestValue().getValue()));
                } else {
                    System.out.println("zero value");
                    return new StockItem(input, 0);
                }
            }
        }).toList();

        for (StockItem item : stockItems) {
            createStock(item);
        }
    }

    private void fetchAndSaveMonthlyStockCountDay(User user) {
        Integer day = lmisServer.getDayForMonthlyStockCount(user);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MONTHLY_STOCK_COUNT_DAY, day);
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

    private void createActionValue(final CommodityActionValue actionValue) {
        dbUtil.withDao(CommodityActionValue.class, new Operation<CommodityActionValue, Void>() {
            @Override
            public Void operate(Dao<CommodityActionValue, String> dao) throws SQLException {
                dao.createOrUpdate(actionValue);
                return null;
            }
        });
    }

    protected void saveActionValues(List<CommodityActionValue> commodityActionValues) {
        if (commodityActionValues != null) {
            for (CommodityActionValue actionValue : commodityActionValues) {
                createActionValue(actionValue);
            }
        }
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
        dbUtil.withDao(Category.class, new Operation<Category, Void>() {
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
        dbUtil.withDao(Commodity.class, new Operation<Commodity, Void>() {
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

        for (CommodityAction commodityAction : commodity.getCommodityActivities()) {
            if (commodityAction.getDataSet() != null) {
                dataSetGenericDao.createOrUpdate(commodityAction.getDataSet());
            }
            if (commodityAction.getCommodity() == null) {
                commodityAction.setCommodity(commodity);
            }
            e(Dhis2.SYNC, String.format("Saving activity %s %s", commodityAction.getName(), commodityAction.getId()));
            commodityActivityGenericDao.createOrUpdate(commodityAction);
        }

    }


}
