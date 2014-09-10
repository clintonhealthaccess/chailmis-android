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

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActivity;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.clintonhealthaccess.lmis.app.persistence.DbUtil.Operation;

public class CommodityService {
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

    public void initialise(User user) {
        List<Category> allCommodities = lmisServer.fetchCommodities(user);
        saveToDatabase(allCommodities);
        categoryService.clearCache();

        List<Commodity> commodities = all();
        Map<Commodity, Integer> stockLevels = lmisServer.fetchStockLevels(commodities, user);
        saveStockLevels(stockLevels);

        //FIXME: https://github.com/chailmis/chailmis-android/issues/36
        allocationService.syncAllocations();
        categoryService.clearCache();
    }

    private void createStock(final Commodity commodity, final int amount) {
        dbUtil.withDao(StockItem.class, new Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                StockItem stockItem = new StockItem(commodity, amount);
                dao.create(stockItem);
                return null;
            }
        });
    }

    private void saveStockLevels(Map<Commodity, Integer> stockLevels) {
        for (Commodity commodity : all()) {
            if (stockLevels.containsKey(commodity)) {
                createStock(commodity, stockLevels.get(commodity));
            } else {
                createStock(commodity, 0);
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
                    createCommodityActivity(commodity);
                }
                return null;
            }
        });
    }

    private void createCommodityActivity(Commodity commodity) {
        GenericDao<CommodityActivity> commodityActivityGenericDao = new GenericDao<>(CommodityActivity.class, context);
        GenericDao<DataSet> dataSetGenericDao = new GenericDao<>(DataSet.class, context);

        for (CommodityActivity commodityActivity : commodity.getCommodityActivities()) {
            if (commodityActivity.getDataSet() != null) {
                dataSetGenericDao.createOrUpdate(commodityActivity.getDataSet());
            }
            if (commodityActivity.getCommodity() == null) {
                commodityActivity.setCommodity(commodity);
            }
            commodityActivityGenericDao.create(commodityActivity);
        }

    }


}
