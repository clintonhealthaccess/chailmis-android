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

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.StockItemSnapshot;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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



            commodityService.initialise(new

            User("test","pass")

            );
            categories=categoryService.all();
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

        SimpleDateFormat dateFormatYear = new SimpleDateFormat("YYYY");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM");
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

        SimpleDateFormat dateFormatYear = new SimpleDateFormat("YYYY");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM");

        createReceive(commodity, endDate, 20);
        createReceive(commodity, endDate, 30);

        List<FacilityStockReportItem> facilityStockReportItems = reportsService.getFacilityReportItemsForCategory(category, dateFormatYear.format(startDate),
                dateFormatMonth.format(startDate), dateFormatYear.format(endDate), dateFormatMonth.format(endDate));

        assertThat(facilityStockReportItems.get(0).getCommoditiesReceived(), is(50));
    }


    private void createReceive(Commodity commodity, Date date, int quantityReceived) {
        Receive receive = new Receive();

        ReceiveItem receiveItem = new ReceiveItem();
        receiveItem.setCommodity(commodity);
        receiveItem.setQuantityAllocated(quantityReceived);
        receiveItem.setQuantityReceived(quantityReceived);

        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);
    }

    private void createStockItemSnapshot(Commodity commodity, Date time, int difference) {
        StockItemSnapshot stockItemSnapshot = new StockItemSnapshot(commodity, time, commodity.getStockOnHand() + difference);
        new GenericDao<StockItemSnapshot>(StockItemSnapshot.class, application).create(stockItemSnapshot);
    }
}