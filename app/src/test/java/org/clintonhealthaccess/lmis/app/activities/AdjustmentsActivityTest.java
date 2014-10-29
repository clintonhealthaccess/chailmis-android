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
import android.widget.TextView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import junit.framework.TestCase;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.AdjustmentsViewModel;
import org.clintonhealthaccess.lmis.app.adapters.AdjustmentsAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.AdjustmentService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.ListTestUtils;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class AdjustmentsActivityTest extends TestCase {

    @Inject
    private CommodityService commodityService;
    private AdjustmentService adjustmentService;
    private UserService userService;
    private StockService stockService;


    public static AdjustmentsActivity getAdjustmentsActivity() {
        return setupActivity(AdjustmentsActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        stockService = mock(StockService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjectionWithMockLmisServer(application, this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(stockService);
                bind(UserService.class).toInstance(userService);
            }
        });
        commodityService.initialise(new User("test", "pass"));
    }

    @Test
    public void testBuildActivity() throws Exception {
        AdjustmentsActivity adjustmentsActivity = getAdjustmentsActivity();
        assertThat(adjustmentsActivity, not(nullValue()));
    }

    @Test
    public void shouldHaveAKeyBoardView() throws Exception {
        AdjustmentsActivity adjustmentsActivity = getAdjustmentsActivity();
        assertThat(adjustmentsActivity.keyBoardView, not(nullValue()));
    }

    @Test
    public void shouldSetupAdjustmentReasons() throws Exception {
        AdjustmentsActivity adjustmentsActivity = getAdjustmentsActivity();
        assertThat(adjustmentsActivity.spinnerAdjustmentReason.getAdapter().getCount(), is(greaterThan(0)));
    }

    @Test
    public void shouldChangeTypeInAdapterWhenReasonChanges() throws Exception {
        AdjustmentsActivity adjustmentsActivity = getAdjustmentsActivity();

        List<Commodity> commodityList = commodityService.all();
        adjustmentsActivity.onEvent(new CommodityToggledEvent(new AdjustmentsViewModel(commodityList.get(0))));
        adjustmentsActivity.onEvent(new CommodityToggledEvent(new AdjustmentsViewModel(commodityList.get(1))));

        String reason = "Received from another facility";
        assertThat(((AdjustmentReason) adjustmentsActivity.spinnerAdjustmentReason.getAdapter().getItem(2)).getName(), is(reason));
        adjustmentsActivity.spinnerAdjustmentReason.setSelection(2);

        AdjustmentsViewModel adjustmentsViewModel = (AdjustmentsViewModel) adjustmentsActivity.selectedCommodities.get(1);
        assertThat(adjustmentsViewModel.getAdjustmentReason().getName(), is(reason));
    }

    @Test
    public void shouldSetAdjustmentReasonFromIntent() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(AdjustmentsActivity.ADJUSTMENT_REASON, AdjustmentReason.PHYSICAL_COUNT_TEXT);
        AdjustmentsActivity adjustmentsActivity = Robolectric.buildActivity(AdjustmentsActivity.class).withIntent(intent).create().start().resume().visible().get();
        assertThat(adjustmentsActivity.spinnerAdjustmentReason.getSelectedItem().toString(), is(AdjustmentReason.PHYSICAL_COUNT_TEXT));
    }

    @Test
    public void shouldShowErrorMessageForNonDevicesWhenReturnToLGAIsSelected() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();

        Commodity commodity = mock(Commodity.class);
        when(commodity.isDevice()).thenReturn(false);

        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);

        AdjustmentsActivity activity = getAdjustmentsActivity();
        AdjustmentsAdapter adjustmentsAdapter = new AdjustmentsAdapter(activity, R.layout.selected_adjustment_list_item, commodities);

        View row = ListTestUtils.getRowFromListView(0, adjustmentsAdapter, R.layout.selected_adjustment_list_item);
        TextView textView = (TextView) row.findViewById(R.id.textViewCommodityName);
        assertThat(textView.getError(), is(Matchers.nullValue()));

        activity.spinnerAdjustmentReason.setSelection(4);

        View row2 = ListTestUtils.getRowFromListView(0, adjustmentsAdapter, R.layout.selected_adjustment_list_item);
        textView = (TextView) row2.findViewById(R.id.textViewCommodityName);
        ANDROID.assertThat(textView).hasError();

    }

    @Ignore("WIP-JOB")
    @Test
    public void shouldShowErrorToastIfNotAVaccineDevice() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();

        Commodity commodity = mock(Commodity.class);
        when(commodity.isDevice()).thenReturn(false);
        when(commodity.getName()).thenReturn("Panadol");

        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.RETURNED_TO_LGA);
        commodities.add(adjustmentsViewModel);

        AdjustmentsActivity activity = getAdjustmentsActivity();
        EventBus.getDefault().post(new CommodityToggledEvent(adjustmentsViewModel));
        activity.spinnerAdjustmentReason.setSelection(4);

        Button submitButton = activity.getSubmitButton();
        submitButton.performClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.not_a_vaccine)));
    }

}