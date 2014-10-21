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

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class MonthlyVaccineUtilizationReportActivityTest {

    private UserService userService;
    private ReportsService reportsService;

    public static MonthlyVaccineUtilizationReportActivity getActivity() {
        Intent intent = new Intent();
        intent.putExtra(ReportsActivity.CATEGORY_BUNDLE_KEY, new Category("food"));
        return Robolectric.buildActivity(MonthlyVaccineUtilizationReportActivity.class).withIntent(intent).create().start().resume().visible().get();
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        reportsService = mock(ReportsService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        when(reportsService.getFacilityReportItemsForCategory((Category) any(), anyString(), anyString(), anyString(), anyString())).thenReturn(new ArrayList<FacilityStockReportItem>());
        setUpInjectionWithMockLmisServer(application, this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(ReportsService.class).toInstance(reportsService);
            }
        });
    }

    @Test
    public void shouldCreateYearSpinner() throws Exception {
        MonthlyVaccineUtilizationReportActivity activity = getActivity();
        ANDROID.assertThat(activity.spinnerYear).isNotNull();
        ANDROID.assertThat(activity.spinnerYear).isVisible();
    }

    @Test
    public void shouldCreateMonthSpinner() throws Exception {
        MonthlyVaccineUtilizationReportActivity activity = getActivity();
        ANDROID.assertThat(activity.spinnerMonth).isNotNull();
        ANDROID.assertThat(activity.spinnerMonth).isVisible();
    }
}
