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
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class CommodityActionServiceTest {

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;
    @Inject
    private CommodityService commodityService;
    @Inject
    private CommodityActionService commodityActionService;

    @Before
    public void setUp() throws Exception {
        mockLmisServer = mock(LmisServer.class);
        mockStockLevels = testActionValues(application);
        //when(mockLmisServer.fetchCommodities((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCategories((User) anyObject())).thenReturn(defaultCategories(application));
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
    public void shouldReturnAMCForAGivenPeriod() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.AMC);
        assertThat(amc, is(103));
    }

    @Test
    public void shouldReturnCorrectAMCForGivenPeriod() throws Exception {
        Commodity commodity = commodityService.all().get(1);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.AMC);
        assertThat(amc, is(125));

    }

    @Test
    public void shouldReturnCorrectAMCForTwoPeriods() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.AMC);
        assertThat(amc, is(119));

    }

    @Test
    public void shouldReturnCorrectAMCForThreePeriods() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.JUNE, 07);
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.AMC);
        assertThat(amc, is(79));
    }


    @Test
    public void shouldReturnMaximumThresholdForAGivenPeriod() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        int maxThreshold = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.MAXIMUM_THRESHOLD);
        assertThat(maxThreshold, is(40));
    }

    @Test
    public void shouldReturnCorrectMaximumThresholdForGivenPeriod() throws Exception {
        Commodity commodity = commodityService.all().get(1);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.MAXIMUM_THRESHOLD);
        assertThat(amc, is(47));

    }

    @Test
    public void shouldReturnCorrectMaximumThresholdForTwoPeriods() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.MAY, 07);
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.MAXIMUM_THRESHOLD);
        assertThat(amc, is(50));

    }

    @Test
    public void shouldReturnCorrectMaximumThresholdForThreePeriods() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.APRIL, 01);
        Date startDate = calendar.getTime();

        calendar.set(2014, Calendar.JUNE, 07);
        Date endDate = calendar.getTime();

        int amc = commodityActionService.getMonthlyValue(commodity, startDate, endDate, DataElementType.MAXIMUM_THRESHOLD);
        assertThat(amc, is(33));
    }

}