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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.LmisTestClass;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.LMISTestCase.createStockItemSnapshot;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.lose;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.receive;
import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class StockItemSnapshotServiceTest extends LmisTestClass {

    @Inject
    private CommodityService commodityService;
    @Inject
    private StockService stockService;

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;
    @Inject
    private StockItemSnapshotService stockItemSnapshotService;
    @Inject
    private ReceiveService receiveService;
    @Inject
    private LossService lossService;

    @Before
    public void setUp() throws Exception {
        mockLmisServer = mock(LmisServer.class);
        mockStockLevels = testActionValues(application);
        //when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCategories((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCommodityActionValues((User) anyObject())).thenReturn(mockStockLevels);

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
        stockService.increaseStockLevelFor(commodity, increase, new Date());

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
        stockService.increaseStockLevelFor(commodity, increase, new Date());

        StockItemSnapshot stockItemSnapshot = stockItemSnapshotService.get(commodity, new Date());
        int expectedQuantity = initialQuantity + increase;
        assertThat(stockItemSnapshot.getQuantity(), is(expectedQuantity));
    }

    @Test
    public void shouldCreateStockItemSnapshotWhenStockLevelIsReduced() throws Exception {
        Commodity commodity = commodityService.all().get(1);
        int initialQuantity = commodity.getStockOnHand();

        int decrease = 6;
        stockService.reduceStockLevelFor(commodity, decrease, new Date());

        StockItemSnapshot stockItemSnapshot = stockItemSnapshotService.get(commodity, new Date());
        int expectedQuantity = initialQuantity - decrease;
        assertThat(stockItemSnapshot.getQuantity(), is(expectedQuantity));
    }

    @Test
    public void shouldCreateSingleSnapshotForEachDayAndNotThrowAnException() throws Exception {
        Commodity commodity = commodityService.all().get(1);

        int decrease = 6;
        int increase = 10;
        stockService.reduceStockLevelFor(commodity, decrease, new Date());
        stockService.increaseStockLevelFor(commodity, increase, new Date());

        stockItemSnapshotService.get(commodity, new Date());
    }

    @Test
    public void shouldReturnLatestAvailableStockItemSnapshot() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date snapshotDate = calendar.getTime();

        StockItemSnapshot stockItemSnapshot = createStockItemSnapshot(commodity, snapshotDate, 10);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date date2DaysAhead = calendar.getTime();
        StockItemSnapshot latestSnapshot = stockItemSnapshotService.getLatest(commodity, date2DaysAhead);

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
        int initialQuantity = commodity.getStockOnHand();

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date twoDaysAhead = calendar.getTime();

        int difference = -initialQuantity;
        createStockItemSnapshot(commodity, twoDaysAhead, difference);

        calendar.add(Calendar.DAY_OF_MONTH, 8);
        Date tenDaysAhead = calendar.getTime();

        int numOfStockOutDays = stockItemSnapshotService.getStockOutDays(commodity, startDate, tenDaysAhead);

        assertThat(numOfStockOutDays, is(9));
    }

    @Test
    public void shouldReturnMinimumAndMaximumStockValuesForACommodityOnADay() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int initialStock = commodity.getStockOnHand();

        Date today = DateUtil.today();
        Date stockDay = DateUtil.addDayOfMonth(today, -5);

        receive(commodity, 150, receiveService, stockDay);
        lose(commodity, 152, lossService, stockDay);
        receive(commodity, 50, receiveService, stockDay);

        StockItemSnapshot stockItemSnapshot = stockItemSnapshotService.get(commodity, stockDay);

        int expectedMaxStockLevel = initialStock + 150;
        assertThat(stockItemSnapshot.minimumStockLevel(), is(expectedMaxStockLevel - 152));
        assertThat(stockItemSnapshot.maximumStockLevel(), is(expectedMaxStockLevel));
    }

    @Test
    public void shouldReturnWhetherADayHadAStockOut() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int initialStock = commodity.getStockOnHand();

        Date today = DateUtil.today();
        lose(commodity, initialStock, lossService, today);
        receive(commodity, 145, receiveService, today);

        assertThat(stockItemSnapshotService.isStockOutDay(today, commodity), is(true));

    }

    @Test
    public void shouldAlsoReturnCorrectNumberOfStockOutDays() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int initialQuantity = commodity.getStockOnHand();

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 4);
        lose(commodity, initialQuantity, lossService, calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 2);
        receive(commodity, 100, receiveService, calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 3);
        lose(commodity, 100, lossService, calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 11);
        int numOfStockOutDays = stockItemSnapshotService.getStockOutDays(commodity, startDate, calendar.getTime());

        assertThat(numOfStockOutDays, is(14));
    }

}
