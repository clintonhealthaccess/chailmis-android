package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class DispensingServiceTest {
    @Inject
    private CommodityService commodityService;
    @Inject
    private DispensingService dispensingService;
    @Inject
    private DbUtil dbUtil;
    @Inject
    private StockService stockService;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
    }

    @Test
    public void testSaveDispensingSavesItsDispensingItems() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        Dispensing dispensing = new Dispensing();
        DispensingItem newDispensingItem = new DispensingItem(commodity, 1);
        dispensing.addItem(newDispensingItem);
        dispensingService.addDispensing(dispensing);

        Long count = dbUtil.withDao(DispensingItem.class, new DbUtil.Operation<DispensingItem, Long>() {
            @Override
            public Long operate(Dao<DispensingItem, String> dao) throws SQLException {
                return dao.countOf();
            }
        });

        assertThat(count, is(1L));


    }

    @Test
    public void addDispensingShouldReduceTheStockLevels() {
        Commodity commodity = commodityService.all().get(0);
        int stockLevel = stockService.getStockLevelFor(commodity);
        assertThat(stockLevel, is(10));

        DispensingItem item1 = new DispensingItem(commodity, 1);
        Dispensing dispensing = new Dispensing();
        dispensing.addItem(item1);
        dispensingService.addDispensing(dispensing);

        stockLevel = stockService.getStockLevelFor(commodity);
        assertThat(stockLevel, is(9));
    }

    @Test
    public void shouldGeneratePrescriptionIdForEachDispensingToPatient() throws Exception {
        assertThat(dispensingService.getNextPrescriptionId(), is(notNullValue()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
        String currentMonth = simpleDateFormat.format(new Date());
        assertThat(dispensingService.getNextPrescriptionId(), is(String.format("0001-%s", currentMonth)));
        for (int i = 0; i < 20; i++) {
            Dispensing dispensing = new Dispensing();
            dispensing.setDispenseToFacility(false);
            dispensingService.addDispensing(dispensing);
        }
        assertThat(dispensingService.getNextPrescriptionId(), is(String.format("0021-%s", currentMonth)));
    }
}