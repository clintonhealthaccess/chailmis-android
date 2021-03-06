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
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.adjust;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class AdjustmentServiceTest extends LmisTestClass {

    @Inject
    CommodityService commodityService;

    @Inject
    AdjustmentService adjustmentService;

    AlertsService alertsService;

    CommoditySnapshotService commoditySnapShotService;

    StockService stockService;

    @Before
    public void setUp() throws Exception {
        alertsService = mock(AlertsService.class);
        commoditySnapShotService = mock(CommoditySnapshotService.class);
        stockService = mock(StockService.class);
        setUpInjectionWithMockLmisServer(Robolectric.application, this, new AbstractModule() {
            @Override
            public void configure() {
                bind(AlertsService.class).toInstance(alertsService);
                bind(CommoditySnapshotService.class).toInstance(commoditySnapShotService);
                bind(StockService.class).toInstance(stockService);
            }
        });
        commodityService.initialise(new User("user", "pass"));
    }

    @Test
    public void shouldSaveAdjustments() throws Exception {
        GenericDao<Adjustment> adjustmentGenericDao = new GenericDao<>(Adjustment.class, Robolectric.application);

        final Commodity commodity = commodityService.all().get(0);
        int initialCount = adjustmentGenericDao.queryForAll().size();

        List<Adjustment> adjustments = Arrays.asList(
                new Adjustment(commodity, 5, true, "Sent to another facility")
        );
        adjustmentService.save(adjustments);
        int finalCount = adjustmentGenericDao.queryForAll().size();
        assertEquals(finalCount, initialCount + 1);

    }


    @Test
    public void shouldCreateSnapShotsForSavedAdjustments() throws Exception {
        final Commodity commodity = commodityService.all().get(0);
        Adjustment adjustment = new Adjustment(commodity, 5, true, "Sent to another facility");
        List<Adjustment> adjustments = Arrays.asList(
                adjustment
        );
        adjustmentService.save(adjustments);
        verify(commoditySnapShotService, atLeastOnce()).add(adjustment);

    }

    @Test
    public void shouldIncreaseStockLevelForPositiveAdjustment() throws Exception {
        final Commodity commodity = commodityService.all().get(0);
        Adjustment adjustment = new Adjustment(commodity, 5, true, "Sent to another facility");
        List<Adjustment> adjustments = Arrays.asList(
                adjustment
        );
        adjustmentService.save(adjustments);
        verify(stockService, atLeastOnce()).increaseStockLevelFor(adjustment.getCommodity(), 5, adjustment.getCreated());

    }

    @Test
    public void shouldReduceStockLevelForNegativeAdjustment() throws Exception {
        final Commodity commodity = commodityService.all().get(0);
        Adjustment adjustment = new Adjustment(commodity, 5, false, "Sent to another facility");
        List<Adjustment> adjustments = Arrays.asList(
                adjustment
        );
        adjustmentService.save(adjustments);
        verify(stockService, atLeastOnce()).reduceStockLevelFor(adjustment.getCommodity(), 5, adjustment.getCreated());
    }

    @Test
    public void shouldDisableAllMonthlyStockAlerts() throws Exception {

        final Commodity commodity = commodityService.all().get(0);
        List<Adjustment> adjustments = Arrays.asList(
                new Adjustment(commodity, 5, true, "Sent to another facility")
        );
        adjustmentService.save(adjustments);
        verify(alertsService).disableAllMonthlyStockCountAlerts();
    }

    @Test
    public void shouldReturnTotalQuantityAdjusted() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        adjust(commodity, 2, true, AdjustmentReason.PHYSICAL_COUNT, adjustmentService);
        adjust(commodity, 5, false, AdjustmentReason.SENT_TO_ANOTHER_FACILITY, adjustmentService);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalAdjustments = adjustmentService.totalAdjustment(commodity, startingDate, endDate);
        assertThat(totalAdjustments, is(-3));
    }

    @Test
    public void shouldReturnCorrectTotalQuantityAdjusted() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        adjust(commodity, 40, true, AdjustmentReason.PHYSICAL_COUNT, adjustmentService);
        adjust(commodity, 5, false, AdjustmentReason.SENT_TO_ANOTHER_FACILITY, adjustmentService);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalAdjustments = adjustmentService.totalAdjustment(commodity, startingDate, endDate);
        assertThat(totalAdjustments, is(35));
    }

    @Test
    public void shouldReturnQuantityAdjustedGivenReason() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        adjust(commodity, 40, true, AdjustmentReason.PHYSICAL_COUNT, adjustmentService);
        adjust(commodity, 5, false, AdjustmentReason.SENT_TO_ANOTHER_FACILITY, adjustmentService);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int physicalAdjustments = adjustmentService.totalAdjustment(commodity, startingDate, endDate, AdjustmentReason.PHYSICAL_COUNT);
        assertThat(physicalAdjustments, is(40));

        int sentToFacility = adjustmentService.totalAdjustment(commodity, startingDate, endDate, AdjustmentReason.SENT_TO_ANOTHER_FACILITY);
        assertThat(sentToFacility, is(5));
    }

    @Test
    public void shouldReturnQuantityAdjustedWhenReturnedToLGA() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        adjust(commodity, 40, true, AdjustmentReason.RETURNED_TO_LGA, adjustmentService);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int returnedAdjustments = adjustmentService.totalAdjustment(commodity, startingDate, endDate, AdjustmentReason.RETURNED_TO_LGA);
        assertThat(returnedAdjustments, is(40));
    }
}