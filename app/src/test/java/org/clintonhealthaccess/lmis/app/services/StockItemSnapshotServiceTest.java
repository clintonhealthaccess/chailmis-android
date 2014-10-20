/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.LMISTestCase.createStockItemSnapshot;
import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class StockItemSnapshotServiceTest {

    @Inject
    private CommodityService commodityService;
    @Inject
    private StockService stockService;

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;
    @Inject
    private StockItemSnapshotService stockItemSnapshotService;

    @Before
    public void setUp() throws Exception {
        mockLmisServer = mock(LmisServer.class);
        mockStockLevels = testActionValues(application);
        when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCommodityActionValues(anyList(), (User) anyObject())).thenReturn(mockStockLevels);

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        });

        commodityService.initialise(new User("test", "pass"));
    }

    @Test
    public void shouldReturnStockItemSnapshot() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int initialQuantity = commodity.getStockOnHand();

        int increase = 5;
        stockService.increaseStockLevelFor(commodity, increase);

        StockItemSnapshot stockItemSnapshot = stockItemSnapshotService.get(commodity, new Date());

        assertThat(stockItemSnapshot, is(notNullValue()));

        int expectedQuantity = initialQuantity + increase;
        assertThat(stockItemSnapshot.getQuantity(), is(expectedQuantity));
    }

    @Test
    public void shouldReturnStockItemSnapshotWithCorrectValuesWhenStockLevelIsIncreased() throws Exception {
        Commodity commodity = commodityService.all().get(1);
        int initialQuantity = commodity.getStockOnHand();

        int increase = 5;
        stockService.increaseStockLevelFor(commodity, increase);

        StockItemSnapshot stockItemSnapshot = stockItemSnapshotService.get(commodity, new Date());
        int expectedQuantity = initialQuantity + increase;
        assertThat(stockItemSnapshot.getQuantity(), is(expectedQuantity));
    }

    @Test
    public void shouldCreateStockItemSnapshotWhenStockLevelIsReduced() throws Exception {
        Commodity commodity = commodityService.all().get(1);
        int initialQuantity = commodity.getStockOnHand();

        int decrease = 6;
        stockService.reduceStockLevelFor(commodity, decrease);

        StockItemSnapshot stockItemSnapshot = stockItemSnapshotService.get(commodity, new Date());
        int expectedQuantity = initialQuantity - decrease;
        assertThat(stockItemSnapshot.getQuantity(), is(expectedQuantity));
    }

    @Test
    public void shouldCreateSingleSnapshotForEachDayAndNotThrowAnException() throws Exception {
        Commodity commodity = commodityService.all().get(1);

        int decrease = 6;
        int increase = 10;
        stockService.reduceStockLevelFor(commodity, decrease);
        stockService.increaseStockLevelFor(commodity, increase);

        stockItemSnapshotService.get(commodity, new Date());
    }

    @Test
    public void shouldReturnLatestAvailableStockItemSnapshot() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date snapshotDate = calendar.getTime();

        StockItemSnapshot stockItemSnapshot = createStockItemSnapshot(commodity, snapshotDate, 10);

        Date currentDate = new Date();
        StockItemSnapshot latestSnapshot = stockItemSnapshotService.getLatest(commodity, currentDate);

        assertThat(latestSnapshot, is(notNullValue()));
        assertThat(latestSnapshot, is(stockItemSnapshot));
    }

    @Test
    public void shouldReturnCorrectStockOutDays() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int difference = -5;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        calendar.add(Calendar.DAY_OF_MONTH, 4);
        difference = -10;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        int expectedNumOfStockOutDays = 4;

        int numOfStockOutDays = stockItemSnapshotService.getStockOutDays(commodity, startDate, endDate);

        assertThat(numOfStockOutDays, is(expectedNumOfStockOutDays));
    }

    @Test
    public void shouldReturnStockOutDays() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int difference = -5;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        int stockOutDay = 8;
        calendar.add(Calendar.DAY_OF_MONTH, stockOutDay);
        difference = -10;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        int expectedNumOfStockOutDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - (stockOutDay + 1);

        int numOfStockOutDays = stockItemSnapshotService.getStockOutDays(commodity, startDate, endDate);

        assertThat(numOfStockOutDays, is(expectedNumOfStockOutDays));
    }
}
