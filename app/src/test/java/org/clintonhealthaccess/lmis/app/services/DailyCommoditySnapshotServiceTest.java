package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Aggregation;
import org.clintonhealthaccess.lmis.app.models.AggregationField;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.DailyCommoditySnapshot;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class DailyCommoditySnapshotServiceTest {
    @Inject
    DailyCommoditySnapshotService dailyCommoditySnapshotService;

    @Inject
    CommodityService commodityService;

    @Inject
    DbUtil dbUtil;
    private GenericDao<Category> categoryDao;
    private GenericDao<Aggregation> aggregationDao;
    private GenericDao<AggregationField> aggregationFieldDao;
    private GenericDao<Commodity> commodityDao;
    private GenericDao<DailyCommoditySnapshot> cummulativeDao;


    @Before
    public void setUp() {
        Context context = Robolectric.application;
        categoryDao = new GenericDao<>(Category.class, context);
        aggregationDao = new GenericDao<>(Aggregation.class, context);
        aggregationFieldDao = new GenericDao<>(AggregationField.class, context);
        commodityDao = new GenericDao<>(Commodity.class, context);
        cummulativeDao = new GenericDao<>(DailyCommoditySnapshot.class, context);
        try {
            setUpInjectionWithMockLmisServer(context, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldCreateNewDailyCommoditySnapshotIfNotExist() throws SQLException {
        generateTestCommodities();

        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Commodity fetchedCommodity2 = commodityDao.queryForAll().get(1);

        dailyCommoditySnapshotService.add(new DispensingItem(fetchedCommodity1, 3));
        dailyCommoditySnapshotService.add(new DispensingItem(fetchedCommodity2, 4));

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = cummulativeDao.queryForAll();

        assertThat(dailyCommoditySnapshots.size(), is(2));
        assertThat(dailyCommoditySnapshots.get(0).getValue(), is(3));
    }

    @Test
    public void shouldUpdateDailyCommoditySummaryIfItExists() throws Exception {
        generateTestCommodities();
        Commodity fetchedCommodity = commodityDao.queryForAll().get(0);
        Snapshotable dispensingItem = new DispensingItem(fetchedCommodity, 3);

        dailyCommoditySnapshotService.add(dispensingItem);
        dailyCommoditySnapshotService.add(dispensingItem);
        dailyCommoditySnapshotService.add(dispensingItem);

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = cummulativeDao.queryForAll();
        assertThat(dailyCommoditySnapshots.size(), is(1));
        assertThat(dailyCommoditySnapshots.get(0).getValue(), is(9));
    }

    @Test
    public void shouldMarkSyncedItemAsUnSyncedWhenAnUpdateOccurs() throws Exception {

        generateTestCommodities();
        Commodity fetchedCommodity = commodityDao.queryForAll().get(0);
        Snapshotable dispensingItem = new DispensingItem(fetchedCommodity, 3);


        DailyCommoditySnapshot dailyCommoditySnapshot = new DailyCommoditySnapshot(fetchedCommodity, dispensingItem.getAggregationField(), 3);
        dailyCommoditySnapshot.setSynced(true);
        cummulativeDao.create(dailyCommoditySnapshot);
        dailyCommoditySnapshotService.add(dispensingItem);

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = cummulativeDao.queryForAll();
        assertThat(dailyCommoditySnapshots.size(), is(1));
        assertThat(dailyCommoditySnapshots.get(0).isSynced(), is(false));

    }

    private void generateTestCommodities() {
        Category category = new Category("commodities");
        categoryDao.create(category);

        Aggregation aggregation = new Aggregation();
        aggregation.setName("Consumption");
        aggregation.setId("aggregationId");
        aggregationDao.create(aggregation);

        AggregationField aggregationField = new AggregationField();
        aggregationField.setAggregation(aggregation);
        aggregationField.setName("dispense");
        aggregationField.setId("aggregationFieldId");
        aggregationFieldDao.create(aggregationField);

        Commodity commodity = new Commodity("Panado", category, aggregation);
        Commodity commodity2 = new Commodity("other drug", category, aggregation);
        commodityDao.create(commodity);
        commodityDao.create(commodity2);
    }
}
