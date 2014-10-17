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

import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.LMISTestCase.adjust;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.createStockItemSnapshot;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.dispense;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.lose;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.receive;
import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class ReportsServiceTest {

    @Inject
    CategoryService categoryService;
    @Inject
    private ReportsService reportsService;

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;
    @Inject
    private CommodityService commodityService;
    private List<Category> categories;
    @Inject
    ReceiveService receiveService;
    @Inject
    private DispensingService dispensingService;
    @Inject
    private LossService lossService;
    @Inject
    private AdjustmentService adjustmentService;

    private SimpleDateFormat dateFormatYear;
    private SimpleDateFormat dateFormatMonth;

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
        categories = categoryService.all();

        dateFormatYear = new SimpleDateFormat("YYYY");
        dateFormatMonth = new SimpleDateFormat("MMMM");
    }

    @Ignore@Test
    public void shouldReturnListOfFacilityStockReportItems() throws Exception {
        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(categories.get(0), "2014", "July", "2014", "July");
        assertThat(facilityStockReportItems.size(), is(greaterThan(0)));
    }

    @Ignore@Test
    public void shouldReturnCorrectNumberOfFacilityStockReportItems() throws Exception {
        Category category = categories.get(0);
        int numberOfCommodities = category.getCommodities().size();
        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, "2014", "July", "2014", "July");
        assertThat(facilityStockReportItems.size(), is(numberOfCommodities));

        category = categories.get(1);
        numberOfCommodities = category.getCommodities().size();
        facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, "2014", "July", "2014", "July");
        assertThat(facilityStockReportItems.size(), is(numberOfCommodities));
    }

    @Ignore@Test
    public void shouldReturnCorrectOpeningBalance() throws Exception {

        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int difference = 3;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedQuantity = commodity.getStockOnHand() + difference;
        int openingStock = facilityStockReportItems.get(0).getOpeningStock();
        assertThat(openingStock, is(expectedQuantity));
    }

    @Ignore@Test
    public void shouldReturnCorrectQuantityOfCommoditiesReceived() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        receive(commodity, 20, receiveService);
        receive(commodity, 30, receiveService);

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category,
                dateFormatYear.format(startDate), dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommoditiesReceived(), is(50));
    }

    @Ignore@Test
    public void shouldReturnCorrectQuantityDispensed() throws Exception {
        Category category = categoryService.all().get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        dispense(commodity, 2, dispensingService);
        dispense(commodity, 1, dispensingService);

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category,
                dateFormatYear.format(startDate), dateFormatMonth.format(startDate), dateFormatYear.format(endDate),
                dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommoditiesDispensed(), is(3));
    }

    @Ignore@Test
    public void shouldReturnValidQuantityLost() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        lose(commodity, 2, lossService);
        lose(commodity, 2, lossService);

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category,
                dateFormatYear.format(startDate), dateFormatMonth.format(startDate), dateFormatYear.format(endDate),
                dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommoditiesLost(), is(4));
    }

    @Ignore@Test
    public void shouldReturnTotalQuantityAdjusted() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        adjust(commodity, 6, true, AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY, adjustmentService);
        adjust(commodity, 13, false, AdjustmentReason.PHYSICAL_COUNT, adjustmentService);

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category,
                dateFormatYear.format(startDate), dateFormatMonth.format(startDate), dateFormatYear.format(endDate),
                dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommoditiesAdjusted(), is(-7));

    }

    @Ignore@Test
    public void shouldReturnCorrectStockOnHand() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        calendar.add(Calendar.DAY_OF_MONTH, -2);
        int difference = 3;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(currentDate), dateFormatMonth.format(currentDate));

        int expectedQuantity = commodity.getStockOnHand() + difference;
        int stockOnHand = facilityStockReportItems.get(0).getStockOnHand();
        assertThat(stockOnHand, is(expectedQuantity));
    }

    @Ignore@Test
    public void shouldReturnCorrectAMC() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.APRIL, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedAMC = 103;
        assertThat(facilityStockReportItems.get(0).getCommodityAMC(), is(expectedAMC));
    }

    @Ignore@Test
    public void shouldReturnCorrectAMCFor2Months() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedMaxThreshold = 119;
        assertThat(facilityStockReportItems.get(0).getCommodityAMC(), is(expectedMaxThreshold));
    }

    @Ignore@Test
    public void shouldReturnMaximumThreshold() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.APRIL, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedMaxThreshold = 40;
        assertThat(facilityStockReportItems.get(0).getCommodityMaxThreshold(), is(expectedMaxThreshold));
    }

    @Ignore@Test
    public void shouldReturnCorrectMaximumThreshold() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.MAY, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedMaxThreshold = 60;
        assertThat(facilityStockReportItems.get(0).getCommodityMaxThreshold(), is(expectedMaxThreshold));
    }

    @Ignore@Test
    public void shouldReturnCorrectMaxThresholdFor2Months() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedMaxThreshold = 50;
        assertThat(facilityStockReportItems.get(0).getCommodityMaxThreshold(), is(expectedMaxThreshold));
    }

    @Test
    public void shouldReturnNumberOfStockOutDays() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int difference = -5;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        int stockOutDay = 10;
        calendar.add(Calendar.DAY_OF_MONTH, stockOutDay);
        difference = -10;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        int numOfStockOutDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - (stockOutDay + 1);

        List<FacilityStockReportItem> facilityStockReportItems =
                reportsService.getFacilityReportItemsForCategory(category,
                        dateFormatYear.format(startDate), dateFormatMonth.format(startDate),
                        dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommodityStockOutDays(), is(numOfStockOutDays));
    }


}