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

package org.clintonhealthaccess.lmis.app.activities.reports;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.activities.ReportsActivity;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class FacilityStockReportActivityTest {


    public static final int NUMBER_OF_MONTHS_IN_YEAR = 12;
    private UserService userService;
    private ReportsService reportsService;

    public static FacilityStockReportActivity getActivity() {
        Intent intent = new Intent();
        intent.putExtra(ReportsActivity.CATEGORY_BUNDLE_KEY, new Category("food"));
        return Robolectric.buildActivity(FacilityStockReportActivity.class).withIntent(intent).create().start().resume().visible().get();
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        reportsService = mock(ReportsService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        when(reportsService.getFacilityReportItemsForCategory((Category) any(), anyString(), anyString(), anyString())).thenReturn(new ArrayList<FacilityStockReportItem>());
        setUpInjectionWithMockLmisServer(application, this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(ReportsService.class).toInstance(reportsService);
            }
        });
    }

    @Test
    public void shouldCreateTheYearSpinner() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        ANDROID.assertThat(activity.spinnerStartingYear).isNotNull();
        ANDROID.assertThat(activity.spinnerStartingYear).isVisible();
    }

    @Test
    public void shouldCreateTheEndMonthSpinner() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        ANDROID.assertThat(activity.spinnerEndingMonth).isNotNull();
        ANDROID.assertThat(activity.spinnerEndingMonth).isVisible();
    }

    @Test
    public void shouldCreateTheStartMonthSpinner() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        ANDROID.assertThat(activity.spinnerStartingMonth).isNotNull();
        ANDROID.assertThat(activity.spinnerStartingMonth).isVisible();
    }

    @Test
    public void shouldSet10YearsInTheYearSpinner() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        assertThat(activity.spinnerStartingYear.getAdapter().getCount(), is(10));
    }


    @Test
    public void shouldSet12MonthsInTheStartingMonthSpinner() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        assertThat(activity.spinnerStartingMonth.getAdapter().getCount(), is(NUMBER_OF_MONTHS_IN_YEAR));
    }


    @Test
    public void shouldSet12MonthsInTheEndingMonthSpinner() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        assertThat(activity.spinnerEndingMonth.getAdapter().getCount(), is(NUMBER_OF_MONTHS_IN_YEAR));
    }

    @Test
    public void shouldReturn12Months() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        List<String> months = activity.getMonths();
        System.out.println(months);
        assertThat(months.size(), is(NUMBER_OF_MONTHS_IN_YEAR));

    }

    @Test
    public void shouldSetupListView() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        ANDROID.assertThat(activity.listViewReport).isNotNull();
        ANDROID.assertThat(activity.listViewReport).isVisible();
    }

    @Test
    public void shouldSetupHeaderForListView() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        assertThat(activity.listViewReport.getHeaderViewsCount(), is(greaterThan(0)));
    }

    @Test
    public void shouldSetupAdapterForListView() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        assertThat(activity.listViewReport.getAdapter(), is(notNullValue()));
    }

    @Test
    public void shouldNotAllowYouToSetEndingMonthThatsBeforeTheStartMonth() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        activity.spinnerStartingMonth.setSelection(1);
        assertThat(activity.spinnerStartingMonth.getAdapter().getCount(), is(12));
        assertThat(activity.spinnerEndingMonth.getAdapter().getCount(), is(11));
    }

    @Test
    public void shouldRePopulateEndYearSpinnerWhenStartingYearSpinnerIsChanged() throws Exception {
        FacilityStockReportActivity activity = getActivity();
        activity.spinnerStartingYear.setSelection(3);
        assertThat(activity.spinnerEndingYear.getAdapter().getCount(), is(4));

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        assertThat((String)activity.spinnerEndingYear.getAdapter().getItem(3), is(String.valueOf(year-3)));
    }

}