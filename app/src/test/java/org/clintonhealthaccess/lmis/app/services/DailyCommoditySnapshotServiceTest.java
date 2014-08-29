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

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActivity;
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

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getID;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class DailyCommoditySnapshotServiceTest {
    public static final String DISPENSING = "DISPENSING";
    @Inject
    DailyCommoditySnapshotService dailyCommoditySnapshotService;

    @Inject
    CommodityService commodityService;

    @Inject
    DbUtil dbUtil;
    private GenericDao<Category> categoryDao;
    private GenericDao<Commodity> commodityDao;
    private GenericDao<CommodityActivity> commodityActivityGenericDao;
    private GenericDao<DailyCommoditySnapshot> snapshotDao;


    @Before
    public void setUp() {
        Context context = Robolectric.application;
        categoryDao = new GenericDao<>(Category.class, context);
        commodityDao = new GenericDao<>(Commodity.class, context);
        snapshotDao = new GenericDao<>(DailyCommoditySnapshot.class, context);
        commodityActivityGenericDao = new GenericDao<>(CommodityActivity.class, context);
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

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = snapshotDao.queryForAll();

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

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = snapshotDao.queryForAll();
        assertThat(dailyCommoditySnapshots.size(), is(1));
        assertThat(dailyCommoditySnapshots.get(0).getValue(), is(9));
    }

    @Test
    public void shouldMarkSyncedItemAsUnSyncedWhenAnUpdateOccurs() throws Exception {

        generateTestCommodities();
        Commodity fetchedCommodity = commodityDao.queryForAll().get(0);
        Snapshotable dispensingItem = new DispensingItem(fetchedCommodity, 3);


        DailyCommoditySnapshot dailyCommoditySnapshot = new DailyCommoditySnapshot(fetchedCommodity, dispensingItem.getActivity(), 3);
        dailyCommoditySnapshot.setSynced(true);
        snapshotDao.create(dailyCommoditySnapshot);
        dailyCommoditySnapshotService.add(dispensingItem);

        List<DailyCommoditySnapshot> dailyCommoditySnapshots = snapshotDao.queryForAll();
        assertThat(dailyCommoditySnapshots.size(), is(1));
        assertThat(dailyCommoditySnapshots.get(0).isSynced(), is(false));
    }

    @Test
    public void shouldGetCommoditySnapshotsWithSyncedAsFalse() {
        generateTestCommodities();

        Commodity fetchedCommodity1 = commodityDao.queryForAll().get(0);
        Commodity fetchedCommodity2 = commodityDao.queryForAll().get(1);
        Snapshotable dispensingItem = new DispensingItem(fetchedCommodity1, 3);


        DailyCommoditySnapshot dailyCommoditySnapshot = new DailyCommoditySnapshot(fetchedCommodity1, dispensingItem.getActivity(), 3);
        dailyCommoditySnapshot.setSynced(true);
        snapshotDao.create(dailyCommoditySnapshot);

        dailyCommoditySnapshotService.add(new DispensingItem(fetchedCommodity2, 4));

        List<DailyCommoditySnapshot> unsynchedSnapshots = dailyCommoditySnapshotService.getUnSyncedSnapshots();
        assertThat(unsynchedSnapshots.size(), is(1));
    }

    private void generateTestCommodities() {
        Category category = new Category("commodities");
        categoryDao.create(category);


        Commodity commodity = new Commodity("Panado", category);
        Commodity commodity2 = new Commodity("other drug", category);
        commodityDao.create(commodity);
        commodityDao.create(commodity2);
        CommodityActivity activity = new CommodityActivity(commodity, getID(), "Panado_DISPENSING", DispensingItem.DISPENSE);
        commodityActivityGenericDao.create(activity);
        CommodityActivity activity2 = new CommodityActivity(commodity2, getID(), "other drug_DISPENSING", DispensingItem.DISPENSE);
        commodityActivityGenericDao.create(activity2);
    }


}
