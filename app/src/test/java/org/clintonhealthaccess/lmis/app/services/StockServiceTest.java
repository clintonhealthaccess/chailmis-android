package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static com.j256.ormlite.table.TableUtils.clearTable;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class StockServiceTest {

    @Inject StockService stockService;
    private Dao<StockItem, String> stockDao;
    private AndroidConnectionSource connectionSource;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);

        LmisSqliteOpenHelper openHelper = getHelper(application, LmisSqliteOpenHelper.class);
        connectionSource = new AndroidConnectionSource(openHelper);
        stockDao = createDao(connectionSource, StockItem.class);
    }

    @After
    public void tearDown() throws Exception {
        clearTable(connectionSource, StockItem.class);
        releaseHelper();
    }

    @Test
    public void shouldGetStockCorrespondingToCommodityFromStockTable() throws SQLException {
        Commodity commodity = new Commodity("item name");
        StockItem stockItem = new StockItem(commodity, 100);
        stockDao.create(stockItem);

        int stockLevel = stockService.getStockLevelFor(commodity);

        assertThat(stockLevel, is(100));
    }

}
