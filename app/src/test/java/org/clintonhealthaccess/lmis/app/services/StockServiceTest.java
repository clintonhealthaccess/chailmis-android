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

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.LmisTestClass;
import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static com.j256.ormlite.dao.DaoManager.createDao;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class StockServiceTest extends LmisTestClass {
    @Inject
    StockService stockService;
    @Inject
    CommodityService commodityService;
    @Inject
    DbUtil dbUtil;

    private LmisSqliteOpenHelper openHelper;
    private AndroidConnectionSource connectionSource;

    private Dao<Commodity, String> commodityDao;
    private Dao<StockItem, String> stockDao;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);

        openHelper = LmisSqliteOpenHelper.getInstance(application);// getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        commodityDao = createDao(connectionSource, Commodity.class);
        stockDao = createDao(connectionSource, StockItem.class);
    }

    @Test
    public void shouldGetStockCorrespondingToCommodityFromStockTable() throws SQLException {
        Category category = new Category("test category");
        Commodity commodity = new Commodity("test commodity");
        commodity.setCategory(category);
        commodityDao.create(commodity);

        StockItem stockItem = new StockItem(commodity, 100);
        stockDao.create(stockItem);

        commodity = commodityDao.queryForSameId(commodity);

        int stockLevel = stockService.getStockLevelFor(commodity);
        stockDao.delete(stockItem);

        assertThat(stockLevel, is(100));
    }

    @Test(expected = LmisException.class)
    public void shouldThrowExceptionWhenAskedForStockLevelForNonPersistedCommodity() {
        Commodity notExistCommodity = new Commodity("Some Non-exist Commodity");
        stockService.getStockLevelFor(notExistCommodity);
    }

    @Test
    public void shouldCreateAStockItemRowForEachCommodityOnInitialise() throws SQLException {
        commodityService.initialise(new User("test", "pass"));

        for (Commodity commodity : commodityService.all()) {
            assertThat(stockService.getStockLevelFor(commodity), greaterThanOrEqualTo(0));
        }
    }
}
