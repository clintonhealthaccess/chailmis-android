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

package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.ReportType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class ReportsActivityTest {


    private UserService userService;
    @Inject
    private CommodityService commodityService;
    @Inject
    private CategoryService categoryService;

    private ReportsActivity getReportsActivity() {
        return setupActivity(ReportsActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        when(userService.userRegistered()).thenReturn(true);
        setUpInjectionWithMockLmisServer(Robolectric.application, this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });


    }

    @Test
    public void testBuildActivity() throws Exception {
        ReportsActivity reportsActivity = getReportsActivity();
        assertThat(reportsActivity, not(nullValue()));
    }

    @Test
    public void testCanChangeHeaderText() throws Exception {
        ReportsActivity reportsActivity = getReportsActivity();
        String name = "James";
        reportsActivity.setFacilityName(name);
        assertThat(reportsActivity.textFacilityName.getText().toString(), is(name));

    }


    @Test
    public void testShouldDisplayAllCategoriesAsButtons() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        ReportsActivity reportsActivity = getReportsActivity();

        LinearLayout categoryLayout = (LinearLayout) reportsActivity.findViewById(R.id.layoutCategories);
        int buttonAmount = categoryLayout.getChildCount();
        assertThat(buttonAmount, is(7));

        for (int i = 1; i < buttonAmount; i++) {
            View childView = categoryLayout.getChildAt(i);
            assertThat(childView, instanceOf(Button.class));
        }
    }

    @Test
    public void testThatAnelgesisticsShouldShowTwoReportsWhenClicked() throws Exception {
        commodityService.initialise(new User("test", "pass"));
        ReportsActivity reportsActivity = getReportsActivity();
        LinearLayout categoryLayout = (LinearLayout) reportsActivity.findViewById(R.id.layoutCategories);
        ListView reportButtonsLayout = (ListView) reportsActivity.findViewById(R.id.listViewCategoryReports);
        int buttonAmount = categoryLayout.getChildCount();
        assertThat(buttonAmount, is(7));
        Button button = (Button) categoryLayout.getChildAt(1);
        ANDROID.assertThat(button).hasTextString("Anti Malarials");
        button.performClick();
        assertThat(reportButtonsLayout.getAdapter().getCount(), is(2));
    }

    @Test
    public void shouldLoadReportAcitivityWhenReportButtonIsClicked() throws Exception {

        commodityService.initialise(new User("test", "pass"));
        List<Category> categories = categoryService.all();
        ReportsActivity reportsActivity = getReportsActivity();
        LinearLayout categoryLayout = (LinearLayout) reportsActivity.findViewById(R.id.layoutCategories);
        ListView reportButtonsLayout = (ListView) reportsActivity.findViewById(R.id.listViewCategoryReports);
        Button antiMalarialsButton = (Button) categoryLayout.getChildAt(1);
        antiMalarialsButton.performClick();
        int buttonAmount = reportButtonsLayout.getAdapter().getCount();
        assertThat(buttonAmount, is(2));

        Button reportButton = (Button) reportButtonsLayout.getAdapter().getView(0, null, null);
        reportButton.performClick();

        ReportType facilityStockReport = ReportType.FacilityStockReport;
        Intent intent = new Intent(reportsActivity, facilityStockReport.getReportActivity());
        intent.putExtra(ReportsActivity.CATEGORY_BUNDLE_KEY, categories.get(0));
        assertThat(shadowOf(reportsActivity).getNextStartedActivity(), equalTo(intent));
    }
}
