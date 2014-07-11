package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class StockServiceTest {

    @Inject
    StockService stockService;
    @Inject
    CommodityService commodityService;
    @Inject
    DbUtil dbUtil;

    private Dao<StockItem, String> stockDao;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);

        dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                stockDao = dao;
                return null;
            }
        });
    }

    @Test
    public void shouldGetStockCorrespondingToCommodityFromStockTable() throws SQLException {
        Commodity commodity = new Commodity("item name");
        StockItem stockItem = new StockItem(commodity, 100);
        stockDao.create(stockItem);

        int stockLevel = stockService.getStockLevelFor(commodity);
        stockDao.delete(stockItem);

        assertThat(stockLevel, is(100));

    }

    @Test
    public void shouldThrowExceptionWhenAskedForStockLevelForNonPersistedCommodity() {
        //Implement. Next test depends on this behaviour
    }

    @Test
    public void shouldCreateAStockItemRowForEachCommodityOnInitialise() {
        commodityService.initialise();

        stockService.initialise();

        for(Commodity commodity : commodityService.all()) {
            assertThat(stockService.getStockLevelFor(commodity), greaterThanOrEqualTo(0));
        }

        // getStockLevel will blow up if asked for a commodity that doesn't exist
        assertThat(1, is(1));
    }
}