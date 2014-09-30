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

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.persistence.DbUtil;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestFixture.getDefaultCommodities;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
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
    private CommodityService commodityService;
    private CommodityService spyedCommodityService;
    @Inject
    private DbUtil dbUtil;

    @Inject
    SharedPreferences sharedPreferences;

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;

    @Before
    public void setUp() throws Exception {
        mockLmisServer = mock(LmisServer.class);
        mockStockLevels = testActionValues(application);
        when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCommodityActionValues(anyList(), (User) anyObject())).thenReturn(mockStockLevels);
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

        assertThat(commodities.size(), is(7));
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
        assertThat(actual.getDataSet(), is(notNullValue()));

    }

    @Test
    public void shouldSaveStockLevelsOnInitialise() throws Exception {
        spyedCommodityService = spy(commodityService);
        spyedCommodityService.initialise(new User("user", "user"));
        verify(spyedCommodityService).saveActionValues(mockStockLevels);
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
        for (Commodity commodity : commodities) {
            System.out.println(commodity.getName() + " -- " + commodity.getAMC());
        }
        assertThat(commodities.get(0).getAMC(), is(125));
        assertThat(commodities.get(0).getName(), is("Choloquine"));
    }

    private void verifyAllCommodityCategories() {
        List<Category> allCategories = categoryService.all();

        assertThat(allCategories.size(), is(6));
        Category antiMalarialCategory = allCategories.get(0);
        assertThat(antiMalarialCategory.getName(), equalTo("Anti Malarials"));
        assertThat(antiMalarialCategory.getCommodities().size(), is(6));
        assertThat(antiMalarialCategory.getCommodities().get(0).getName(), equalTo("Coartem"));
    }
}
