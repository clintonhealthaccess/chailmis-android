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

import android.content.SharedPreferences;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationItem;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.LMISTestCase.adjust;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.dispense;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.receive;
import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestFixture.getDefaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class CommodityServiceTest {

    public static final int MOCK_DAY = 15;
    @Inject
    private CategoryService categoryService;
    @Inject
    private DispensingService dispensingService;
    @Inject
    private CommodityService commodityService;
    @Inject
    private ReceiveService receiveService;

    private CommodityService spyedCommodityService;
    private CommodityActionService commodityActionService;
    @Inject
    private DbUtil dbUtil;

    @Inject
    SharedPreferences sharedPreferences;

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;
    @Inject
    private AdjustmentService adjustmentService;

    @Before
    public void setUp() throws Exception {
        mockLmisServer = mock(LmisServer.class);
        commodityActionService = mock(CommodityActionService.class);
        mockStockLevels = testActionValues(application);
        when(mockLmisServer.fetchCategories((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCommodityActionValues((User) anyObject())).thenReturn(mockStockLevels);
        when(mockLmisServer.fetchIntegerConstant((User) anyObject(), anyString())).thenReturn(MOCK_DAY);

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        });


    }

    @Test
    public void testShouldLoadAllCommodityCategories() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        verifyAllCommodityCategories();
    }

    @Test
    public void shouldLoadAllCommodities() throws IOException {
        commodityService.initialise(new User("test", "pass"));
        List<Commodity> expectedCommodities = getDefaultCommodities(application);

        List<Commodity> commodities = commodityService.all();

        assertThat(commodities.size(), is(8));
        for (Commodity commodity : expectedCommodities) {
            assertThat(expectedCommodities, contains(commodity));
        }
    }

    @Test
    public void testShouldPrepareDefaultCommodities() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        verifyAllCommodityCategories();
    }

    @Test
    public void shouldSaveCommodityCommodityActivities() {
        commodityService.initialise(new User("test", "pass"));
        Commodity testCommodity = commodityService.all().get(0);
        assertThat(testCommodity.getCommodityActionsSaved().size(), is(not(0)));
    }


    @Test
    public void shouldSaveCommodityActionWithDataSet() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        Commodity testCommodity = commodityService.all().get(0);
        assertThat(testCommodity.getCommodityActionsSaved().size(), is(not(0)));
        CommodityAction actual = (CommodityAction) testCommodity.getCommodityActionsSaved().toArray()[0];
        assertThat(actual.getCommodityActionDataSets(), is(notNullValue()));
    }

    @Test
    public void shouldSaveStockLevelsOnInitialise() throws Exception {
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
                bind(CommodityActionService.class).toInstance(commodityActionService);
            }
        });
        spyedCommodityService = spy(commodityService);
        spyedCommodityService.initialise(new User("user", "user"));
        verify(commodityActionService).syncCommodityActionValues((User) any());
    }

    @Test
    public void shouldGetMonthlyStockCountDayAndSaveItToPreferences() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        assertThat(sharedPreferences.getInt(CommodityService.MONTHLY_STOCK_COUNT_DAY, 0), is(MOCK_DAY));
    }

    @Test
    public void shouldReturnMostConsumedCommodities() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        categoryService.clearCache();
        List<Commodity> commodities = commodityService.getMost5HighlyConsumedCommodities();

        assertThat(commodities.get(0).getAMC(), is(125));
        assertThat(commodities.get(0).getName(), is("Choloquine"));
    }

    private void verifyAllCommodityCategories() {
        List<Category> allCategories = categoryService.all();

        assertThat(allCategories.size(), is(7));
        Category antiMalarialCategory = allCategories.get(0);
        assertThat(antiMalarialCategory.getName(), equalTo("Anti Malarials"));
        assertThat(antiMalarialCategory.getCommodities().size(), is(6));
        assertThat(antiMalarialCategory.getCommodities().get(0).getName(), equalTo("Coartem"));
    }

    @Test
    public void shouldReturnCorrectNumberOfUtilizationValuesForTheMonthInTheUtilizationItem() throws Exception {
        Commodity commodity = categoryService.all().get(0).getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date date = calendar.getTime();


        List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(
                commodity, date);
        assertThat(utilizationItems.get(0).getUtilizationValues().size(), is(30));

        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date date2 = calendar.getTime();

        List<UtilizationItem> decemberUtilizationItems = commodityService.getMonthlyUtilizationItems(commodity, date2);
        assertThat(decemberUtilizationItems.get(0).getUtilizationValues().size(), is(31));
    }

    @Test
    public void shouldReturnOpeningBalanceUtilizationItemWithCorrectUtilizationValue() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        categoryService.clearCache();
        Commodity commodity = categoryService.all().get(0).getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        int stockOnHand = commodity.getStockOnHand();
        dispense(commodity, 3, dispensingService);

        Date tomorrow = DateUtil.addDayOfMonth(today, 1);

        List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(commodity, tomorrow);

        int expectedOpeningStock = stockOnHand - 3;
        int utilizationValueIndex = DateUtil.dayNumber(tomorrow) - 1;

        assertThat(utilizationItems.get(1).getUtilizationValues().get(utilizationValueIndex).getValue(),
                is(expectedOpeningStock));

        Date furtherDate = DateUtil.addDayOfMonth(tomorrow, 1);
        if (furtherDate.before(DateUtil.addDayOfMonth(DateUtil.getMonthEndDate(tomorrow)))) {
            utilizationValueIndex = DateUtil.dayNumber(furtherDate) - 1;

            assertThat(utilizationItems.get(1).getUtilizationValues().get(utilizationValueIndex).getValue(),
                    is(expectedOpeningStock));
        }
    }


    @Test
    public void shouldReturnClosingBalanceUtilizationItemWithCorrectUtilizationValue() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        categoryService.clearCache();
        Commodity commodity = categoryService.all().get(0).getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        int stockOnHand = commodity.getStockOnHand();
        dispense(commodity, 3, dispensingService);

        List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(commodity, today);

        int expectedClosingStock = stockOnHand - 3;
        int utilizationValueIndex = DateUtil.dayNumber(today) - 1;
        assertThat(utilizationItems.get(4).getUtilizationValues().get(utilizationValueIndex).getValue(),
                is(expectedClosingStock));
    }

    @Test
    public void shouldReturnDosesOpenedUtilizationItemWithCorrectUtilizationValue() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        categoryService.clearCache();

        Commodity commodity = categoryService.all().get(0).getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        dispense(commodity, 2, dispensingService);
        dispense(commodity, 5, dispensingService);

        List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(commodity, today);

        int expectedDosedOpened = 7;
        int utilizationValueIndex = DateUtil.dayNumber(today) - 1;

        assertThat(utilizationItems.get(3).getUtilizationValues().get(utilizationValueIndex).getValue(),
                is(expectedDosedOpened));
    }

    @Test
    public void shouldReturnReceivedUtilizationItemWithCorrectUtilizationValue() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        categoryService.clearCache();

        Commodity commodity = categoryService.all().get(0).getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        receive(commodity, 2, receiveService);
        receive(commodity, 3, receiveService);

        List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(commodity, today);

        int expectedValue = 5;
        int utilizationValueIndex = DateUtil.dayNumber(today) - 1;

        assertThat(utilizationItems.get(2).getUtilizationValues().get(utilizationValueIndex).getValue(),
                is(expectedValue));
    }

    @Test
    public void shouldReturnReturnedToLGAUtilizationItemWithCorrectUtilizationValue() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        categoryService.clearCache();

        Commodity commodity = categoryService.all().get(6).getCommodities().get(0);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        assertNotNull(commodity);
        adjust(commodity, 2, false, AdjustmentReason.RETURNED_TO_LGA, adjustmentService);
        adjust(commodity, 3, false, AdjustmentReason.RETURNED_TO_LGA, adjustmentService);

        List<UtilizationItem> utilizationItems = commodityService.getMonthlyUtilizationItems(commodity, today);

        int expectedValue = 5;
        int utilizationValueIndex = DateUtil.dayNumber(today) - 1;

        assertThat(utilizationItems.get(5).getUtilizationValues().get(utilizationValueIndex).getValue(),
                is(expectedValue));
    }
}
