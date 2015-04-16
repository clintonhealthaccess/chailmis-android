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
import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.LmisTestClass;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.reports.BinCard;
import org.clintonhealthaccess.lmis.app.models.reports.BinCardItem;
import org.clintonhealthaccess.lmis.app.models.reports.ConsumptionValue;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityCommodityConsumptionRH1ReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityConsumptionReportRH2Item;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.MonthlyVaccineUtilizationReportItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.LMISTestCase.adjust;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.createStockItemSnapshot;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.createStockItemSnapshotValue;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.dispense;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.lose;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.receive;
import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class ReportsServiceTest extends LmisTestClass {

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

    @Inject
    private DbUtil dbUtil;
    @Inject
    private StockItemSnapshotService stockItemSnapshotService;

    private SimpleDateFormat dateFormatYear;

    private SimpleDateFormat dateFormatMonth;

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
        categories = categoryService.all();

        dateFormatYear = new SimpleDateFormat("YYYY");
        dateFormatMonth = new SimpleDateFormat("MMMM");
    }

    @Test
    public void shouldReturnListOfFacilityStockReportItems() throws Exception {
        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(categories.get(0), "2014", "July", "2014", "July");
        assertThat(facilityStockReportItems.size(), is(greaterThan(0)));
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void shouldReturnCorrectStockOnHand() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();

        //calendar.add(Calendar.DAY_OF_MONTH, -2);
        int soh = 13;
        stockItemSnapshotService.createOrUpdate(commodity, soh, calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();
        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int stockOnHand = facilityStockReportItems.get(0).getStockOnHand();
        assertThat(stockOnHand, is(soh));
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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
        difference = -10;
        createStockItemSnapshot(commodity, calendar.getTime(), difference);

        int numOfStockOutDays = DateUtil.dayNumber(endDate) - 1;

        List<FacilityStockReportItem> facilityStockReportItems =
                reportsService.getFacilityReportItemsForCategory(category,
                        dateFormatYear.format(startDate), dateFormatMonth.format(startDate),
                        dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommodityStockOutDays(), is(numOfStockOutDays));
    }

    @Test
    public void shouldReturnMinimumThreshold() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.APRIL, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedMinThreshold = 15;
        assertThat(facilityStockReportItems.get(0).getCommodityMinimumThreshold(), is(expectedMinThreshold));
    }

    @Test
    public void shouldReturnCorrectMinimumThresholdFor2Months() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);
        Date endDate = calendar.getTime();

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        int expectedMinThreshold = 17;
        assertThat(facilityStockReportItems.get(0).getCommodityMinimumThreshold(), is(expectedMinThreshold));
    }

    @Test
    public void shouldReturnCorrectFacilityConsumptionRH2Items() throws Exception {
        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int openingStock = 3;
        stockItemSnapshotService.createOrUpdate(commodity, openingStock, calendar.getTime());
        //createStockItemSnapshotValue(commodity, calendar.getTime(), openingStock);

        receive(commodity, 20, receiveService);
        receive(commodity, 30, receiveService);

        dispense(commodity, 2, dispensingService);
        dispense(commodity, 3, dispensingService);

        lose(commodity, 9, lossService);
        lose(commodity, 3, lossService);

        adjust(commodity, 3, false, AdjustmentReason.SENT_TO_ANOTHER_FACILITY, adjustmentService);
        adjust(commodity, 5, false, AdjustmentReason.SENT_TO_ANOTHER_FACILITY, adjustmentService);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        List<FacilityConsumptionReportRH2Item> facilityConsumptionReportRH2Items = reportsService.getFacilityConsumptionReportRH2Items(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        assertThat(facilityConsumptionReportRH2Items.get(0).getOpeningStock(), is(openingStock));
        assertThat(facilityConsumptionReportRH2Items.get(0).getCommoditiesReceived(), is(50));
        assertThat(facilityConsumptionReportRH2Items.get(0).getCommoditiesDispensedToClients(), is(5));

        assertThat(facilityConsumptionReportRH2Items.get(0).getCommoditiesDispensedToFacilities(), is(8));
        assertThat(facilityConsumptionReportRH2Items.get(0).totalDispensed(), is(13));

        assertThat(facilityConsumptionReportRH2Items.get(0).getCommoditiesLost(), is(12));
        assertThat(facilityConsumptionReportRH2Items.get(0).getClosingStock(), is(35));
    }

    @Test
    public void shouldReturnValuesForEachCommodityInTheCategoryForgetFacilityCommodityConsumptionReportRH1() throws Exception {

        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);

        Date endDate = calendar.getTime();
        List<FacilityCommodityConsumptionRH1ReportItem> facilityStockReportItems = reportsService.getFacilityCommodityConsumptionReportRH1(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));
        assertThat(facilityStockReportItems.size(), is(6));
    }


    @Test
    public void shouldCalculateConsumptionWithAllDispensingItemsInDateRange() throws Exception {

        Category category = categories.get(0);
        Commodity commodity = category.getCommodities().get(0);


        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.APRIL, 02);
        createDispensingItemWithDate(commodity, calendar.getTime(), 10);

        calendar.set(2014, Calendar.APRIL, 03);
        createDispensingItemWithDate(commodity, calendar.getTime(), 10);

        calendar.set(2014, Calendar.APRIL, 04);
        createDispensingItemWithDate(commodity, calendar.getTime(), 10);
        createDispensingItemWithDate(commodity, calendar.getTime(), 20);

        calendar.set(2014, Calendar.APRIL, 06);
        Date endDate = calendar.getTime();

        ArrayList<ConsumptionValue> values = reportsService.getConsumptionValuesForCommodityBetweenDates(commodity, startDate, endDate);
        assertThat(values.size(), is(daysBetween(startDate, endDate)));
        assertThat(values.get(0).getConsumption(), is(0));
        assertThat(values.get(1).getConsumption(), is(10));
        assertThat(values.get(2).getConsumption(), is(10));
        assertThat(values.get(3).getConsumption(), is(30));
        assertThat(values.get(4).getConsumption(), is(0));

    }

    private DispensingItem createDispensingItemWithDate(Commodity commodity, Date firstDate, int quantity) {
        Dispensing dispensing = new Dispensing(firstDate);
        new GenericDao(Dispensing.class, application).create(dispensing);
        final DispensingItem dispensingItem = new DispensingItem(commodity, quantity);
        dispensingItem.setDispensing(dispensing);
        dbUtil.withDao(DispensingItem.class, new DbUtil.Operation<DispensingItem, Object>() {
            @Override
            public Object operate(Dao<DispensingItem, String> dao) throws SQLException {
                return dao.create(dispensingItem);
            }
        });
        return dispensingItem;
    }

    public int daysBetween(Date d1, Date d2) {
        return Days.daysBetween(new DateTime(d1), new DateTime(DateUtil.addDayOfMonth(d2, 1))).getDays();
    }

    @Test
    public void shouldReturnCorrectNumberOfMonthlyVaccineUtilizationReportItems() throws Exception {
        Category category = categories.get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date date = calendar.getTime();
        List<MonthlyVaccineUtilizationReportItem> reportItems = reportsService.getMonthlyVaccineUtilizationReportItems(category, dateFormatYear.format(date),
                dateFormatMonth.format(date), false);

        int expectedSize = category.getCommodities().size();
        assertThat(reportItems.size(), is(expectedSize));
    }

    @Test
    public void shouldReturnBinCardForTheCommodity() throws Exception {
        Commodity commodity = categories.get(0).getCommodities().get(0);
        int stock = commodity.getStockOnHand();
        Date date20DaysAgo = DateUtil.addDayOfMonth(new Date(), -20);

        receive(commodity, 200, receiveService, date20DaysAgo);
        dispense(commodity, 30, dispensingService, date20DaysAgo);
        lose(commodity, 20, lossService, date20DaysAgo);

        int expectedMax = commodity.getMaximumThreshold();
        int expectedMin = commodity.getMinimumThreshold();
        int expectedBalance = stock + 150;

        BinCard binCard = reportsService.generateBinCard(commodity);
        assertThat(binCard.getBinCardItems().size(), is(1));
        assertThat(binCard.getMaximumStockLevel(), is(expectedMax));
        assertThat(binCard.getMinimumStockLevel(), is(expectedMin));

        BinCardItem binCardItem = binCard.getBinCardItems().get(0);
        System.out.println("Bin card item is " + binCardItem);
        assertTrue(DateUtil.equal(binCardItem.getDate(), date20DaysAgo));
        assertThat(binCardItem.getQuantityReceived(), is(200));
        assertThat(binCardItem.getQuantityDispensed(), is(30));
        assertThat(binCardItem.getQuantityLost(), is(20));

        assertThat(binCardItem.getStockBalance(), is(expectedBalance));
    }

    @Test
    public void shouldReturnBinCardWithCorrectBinCardItemsForTheCommodity() throws Exception {
        Commodity commodity = categories.get(0).getCommodities().get(0);
        int stock = commodity.getStockOnHand();

        Date date10DaysAgo = DateUtil.addDayOfMonth(new Date(), -10);
        receive(commodity, 200, receiveService, date10DaysAgo);
        dispense(commodity, 30, dispensingService, date10DaysAgo);
        lose(commodity, 20, lossService, date10DaysAgo);

        int expectedBalance10DaysAgo = (stock + 200) - 50;

        Date date8DaysAgo = DateUtil.addDayOfMonth(new Date(), -8);
        dispense(commodity, 25, dispensingService, date8DaysAgo);
        lose(commodity, 10, lossService, date8DaysAgo);

        int expectedMax = commodity.getMaximumThreshold(); // stock + 200;
        int expectedMin = commodity.getMinimumThreshold();
        int expectedBalance8DaysAgo = stock + 115;

        BinCard binCard = reportsService.generateBinCard(commodity);
        assertThat(binCard.getBinCardItems().size(), is(2));
        assertThat(binCard.getMaximumStockLevel(), is(expectedMax));
        assertThat(binCard.getMinimumStockLevel(), is(expectedMin));

        BinCardItem binCardItem = binCard.getBinCardItems().get(0);
        assertTrue(DateUtil.equal(binCardItem.getDate(), date10DaysAgo));
        assertThat(binCardItem.getQuantityReceived(), is(200));
        assertThat(binCardItem.getQuantityDispensed(), is(30));
        assertThat(binCardItem.getQuantityLost(), is(20));

       // BinCardItem binCardItem1 = binCard.getBinCardItems().get(1);

        assertThat(binCardItem.getStockBalance(), is(expectedBalance10DaysAgo));

        BinCardItem binCardItem2 = binCard.getBinCardItems().get(1);
        assertTrue(DateUtil.equal(binCardItem2.getDate(), date8DaysAgo));
        assertThat(binCardItem2.getQuantityReceived(), is(0));
        assertThat(binCardItem2.getQuantityDispensed(), is(25));
        assertThat(binCardItem2.getQuantityLost(), is(10));
        assertThat(binCardItem2.getStockBalance(), is(expectedBalance8DaysAgo));
    }

    @Test
    public void shouldReturnBinCardWithCorrectBinCardItemsForCorrectMinimumAndMaximum() throws Exception {
        Commodity commodity1 = categories.get(0).getCommodities().get(0);

        Date date10DaysAgo = DateUtil.addDayOfMonth(new Date(), -10);
        receive(commodity1, 200, receiveService, date10DaysAgo);
        dispense(commodity1, 130, dispensingService, date10DaysAgo);

        int commodity1ExpectedMax = commodity1.getMaximumThreshold();
        int commodity1ExpectedMin = commodity1.getMinimumThreshold();

        BinCard commodity1BinCard = reportsService.generateBinCard(commodity1);
        assertThat(commodity1BinCard.getMinimumStockLevel(), is(commodity1ExpectedMin));
        assertThat(commodity1BinCard.getMaximumStockLevel(), is(commodity1ExpectedMax));

        Commodity commodity2 = categories.get(0).getCommodities().get(1);

        receive(commodity2, 100, receiveService, date10DaysAgo);
        dispense(commodity2, 90, dispensingService, date10DaysAgo);

        int commodity2ExpectedMax = commodity2.getMaximumThreshold();
        int commodity2ExpectedMin = commodity2.getMinimumThreshold();

        BinCard commodity2BinCard = reportsService.generateBinCard(commodity2);
        assertThat(commodity2BinCard.getMinimumStockLevel(), is(commodity2ExpectedMin));
        assertThat(commodity2BinCard.getMaximumStockLevel(), is(commodity2ExpectedMax));

    }

    @Test
    public void shouldReturnCorrectNumberOfDaysInFebruary2014() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2014, Calendar.FEBRUARY, 01);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        List<Integer> days = reportsService.getDayNumbers(startDate, endDate);

        assertThat(days.size(), is(28));
        assertThat(days.get(0), is(1));
        assertThat(days.get(27), is(28));

    }

    @Test
    public void shouldReturnCorrectNumberOfDaysInOctober() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2014, Calendar.OCTOBER, 01);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        List<Integer> days = reportsService.getDayNumbers(startDate, endDate);

        assertThat(days.size(), is(31));
        assertThat(days.get(0), is(1));
        assertThat(days.get(30), is(31));

    }
}