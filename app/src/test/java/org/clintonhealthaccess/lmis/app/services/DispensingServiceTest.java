package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class DispensingServiceTest {
    @Inject
    DispensingService dispensingService;
    @Inject
    DbUtil dbUtil;
    @Inject
    StockService stockService;

    @Before
    public void setUp() throws SQLException {
        setUpInjection(this);
    }


    @Test
    public void testSaveDispensingSavesItsDispensingItems() throws Exception {

        Commodity food = new Commodity("food");
        DispensingItem item1 = new DispensingItem(food, 1);
        createStockItem(food);
        Dispensing dispensing = new Dispensing();

        dispensing.addItem(item1);

        dispensingService.addDispensing(dispensing);


        Long count = dbUtil.withDao(DispensingItem.class, new DbUtil.Operation<DispensingItem, Long>() {
            @Override
            public Long operate(Dao<DispensingItem, String> dao) throws SQLException {
                return dao.countOf();
            }
        });

        assertThat(count, is(1L));


    }

    private void createStockItem(Commodity food) {
        final StockItem stockItem = new StockItem(food, 100);
        dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {
                dao.create(stockItem);
                return null;
            }
        });
    }

    @Test
    public void addDispensingShouldReduceTheStockLevels() {
        String name = "item name";
        Commodity commodity = new Commodity(name);
        createStockItem(commodity);
        int stockLevel = stockService.getStockLevelFor(commodity);
        assertThat(stockLevel, is(100));

        DispensingItem item1 = new DispensingItem(commodity, 10);

        Dispensing dispensing = new Dispensing();

        dispensing.addItem(item1);

        dispensingService.addDispensing(dispensing);

        stockLevel = stockService.getStockLevelFor(commodity);

        assertThat(stockLevel, is(90));
    }
}