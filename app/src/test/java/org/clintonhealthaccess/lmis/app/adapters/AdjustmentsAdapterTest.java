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

package org.clintonhealthaccess.lmis.app.adapters;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.AdjustmentsActivity;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.AdjustmentsViewModel;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.ListTestUtils;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class AdjustmentsAdapterTest {


    private AdjustmentsActivity activity;

    @Before
    public void setUp() {
        setUpInjection(this);
        activity = setupActivity(AdjustmentsActivity.class);
    }

    @Test
    public void shouldDisableTypeSpinnerForAnyReasonThatIsNotPhysicalStockCount() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY);
        Spinner spinner = (Spinner) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.spinnerAdjustmentType);
        ANDROID.assertThat(spinner).isDisabled();
    }

    @Test
    public void shouldHideTypeSpinnerForPhysicalCount() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.PHYSICAL_COUNT);
        Spinner spinner = (Spinner) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.spinnerAdjustmentType);
        ANDROID.assertThat(spinner).isEnabled();
        ANDROID.assertThat(spinner).isInvisible();
    }

    @Test
    public void shouldShowTheCurrentStockWhenItIsPhysicalCount() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.PHYSICAL_COUNT);
        TextView textView = (TextView) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.textViewCurrentStock);
        ANDROID.assertThat(textView).isVisible();
    }

    @Test
    public void shouldNotShowTheLinearLayoutStockValuesWhenTheReasonIsNotPhysicalCountOrSentToFacility() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY);
        LinearLayout linearLayout = (LinearLayout) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.linearLayoutStockValues);
        ANDROID.assertThat(linearLayout).isGone();
    }

    @Test
    public void shouldShowTheQuantityEditTextWhenTheReasonIsNotPhysicalCount() throws Exception {
        AdjustmentReason adjustmentReason = AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY;
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(adjustmentReason);
        EditText editText = (EditText) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.editTextQuantity);
        ANDROID.assertThat(editText).isVisible();
    }

    @Test
    public void shouldErrorWhenGivingAwayMoreStockThanIsAvailable() throws Exception {
        EditText editText = (EditText) getViewFromListRow(getAdjustmentsAdapterWithQuantityAndPositive(AdjustmentReason.SENT_TO_ANOTHER_FACILITY, 150, false), R.layout.selected_adjustment_commodity_list_item, R.id.editTextQuantity);
        ANDROID.assertThat(editText).isVisible();
        ANDROID.assertThat(editText).hasError();
    }

    @Test
    public void shouldSetAdjustmentViewModelToPositiveForPhysicalStockCountIfQuantityCountedIsGreaterThanSOH() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(50);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);
        EditText editText = (EditText) getViewFromListRow(new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities),
                R.layout.selected_adjustment_commodity_list_item, R.id.editTextQuantity);
        editText.setText("200");
        assertThat(adjustmentsViewModel.getQuantityEntered(), is(200));
        assertThat(adjustmentsViewModel.getPhysicalStockCountDifference(), is(150));
        assertThat(adjustmentsViewModel.isPositive(), is(true));
    }

    @Test
    public void shouldSetAdjustmentViewModelToNegativeForPhysicalStockCountIfQuantityCountedIsLessGrThanSOH() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(100);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);
        EditText editText = (EditText) getViewFromListRow(new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities),
                R.layout.selected_adjustment_commodity_list_item, R.id.editTextQuantity);
        editText.setText("50");
        assertThat(adjustmentsViewModel.getQuantityEntered(), is(50));
        assertThat(adjustmentsViewModel.isPositive(), is(false));
    }

    @Test
    public void shouldSetAdjustmentTypeToPositiveForPhysicalStockCountIfQuantityCountedIsGreaterThanSOH() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(100);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);
        AdjustmentsAdapter adapter = new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities);
        View row = ListTestUtils.getRowFromListView(0, adapter, R.layout.selected_adjustment_commodity_list_item);
        EditText editText = (EditText) row.findViewById(R.id.editTextQuantity);
        Spinner spinnerType = (Spinner) row.findViewById(R.id.spinnerAdjustmentType);
        editText.setText("200");
        assertThat(adjustmentsViewModel.getQuantityEntered(), is(200));
        assertThat(adjustmentsViewModel.getPhysicalStockCountDifference(), is(100));
        assertThat(adjustmentsViewModel.isPositive(), is(true));
        assertThat(spinnerType.getSelectedItem().toString(), is("+"));
    }

    @Test
    public void shouldSetAdjustmentTypeToNegativeForPhysicalStockCountIfCountedIsLessGrThanSOH() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(100);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);
        AdjustmentsAdapter adapter = new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities);

        View row = ListTestUtils.getRowFromListView(0, adapter, R.layout.selected_adjustment_commodity_list_item);
        EditText editText = (EditText) row.findViewById(R.id.editTextQuantity);
        Spinner spinnerType = (Spinner) row.findViewById(R.id.spinnerAdjustmentType);
        editText.setText("50");
        assertThat(adjustmentsViewModel.getQuantityEntered(), is(50));
        assertThat(adjustmentsViewModel.isPositive(), is(false));
        assertThat(spinnerType.getSelectedItem().toString(), is("-"));
    }

    @Test
    public void shouldShowKeyboardWhenTextIsEnteredIntoQuantityField() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(100);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);
        AdjustmentsAdapter adapter = new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities);
        View row = ListTestUtils.getRowFromListView(0, adapter, R.layout.selected_adjustment_commodity_list_item);
        ANDROID.assertThat(activity.keyBoardView).isNotShown();
        EditText editText = (EditText) row.findViewById(R.id.editTextQuantity);
        editText.performClick();
        ANDROID.assertThat(activity.keyBoardView).isShown();
    }

    @Test
    public void shouldSetTheCountedTextViewIfTheCountedIsSetInTheViewModel() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getStockOnHand()).thenReturn(100);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        int stockCounted = 300;
        commodities.add(adjustmentsViewModel);
        AdjustmentsAdapter adapter = new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities);

        View row = ListTestUtils.getRowFromListView(0, adapter, R.layout.selected_adjustment_commodity_list_item);
        EditText editText = (EditText) row.findViewById(R.id.editTextQuantity);
        TextView textView = (TextView) row.findViewById(R.id.textViewDifference);
        editText.setText(Integer.toString(stockCounted));

        ANDROID.assertThat(textView).containsText("200");
    }

    @Test
    public void shouldShowErrorMessageForNonDevicesWhenReturnToLGAIsSelected() throws Exception {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();

        Commodity commodity = mock(Commodity.class);
        when(commodity.isVaccine()).thenReturn(false);

        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, 12, false);
        adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
        commodities.add(adjustmentsViewModel);

        AdjustmentsAdapter adjustmentsAdapter = new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities);

        activity.spinnerAdjustmentReason.setSelection(4);
        View row = ListTestUtils.getRowFromListView(0, adjustmentsAdapter, R.layout.selected_adjustment_commodity_list_item);

        TextView textView = (TextView) row.findViewById(R.id.textViewCommodityName);
        ANDROID.assertThat(textView).hasError();
    }

    private AdjustmentsAdapter getAdjustmentsAdapter(AdjustmentReason adjustmentReason) {
        return getAdjustmentsAdapterForQuantity(adjustmentReason, 12);
    }

    private AdjustmentsAdapter getAdjustmentsAdapterForQuantity(AdjustmentReason adjustmentReason, int quantityEntered) {
        return getAdjustmentsAdapterWithQuantityAndPositive(adjustmentReason, quantityEntered, true);
    }

    private AdjustmentsAdapter getAdjustmentsAdapterWithQuantityAndPositive(AdjustmentReason adjustmentReason, int quantityEntered, boolean positive) {
        ArrayList<AdjustmentsViewModel> commodities = new ArrayList<>();
        Commodity commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn("Food");
        when(commodity.getStockOnHand()).thenReturn(100);
        AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, quantityEntered, positive);
        adjustmentsViewModel.setAdjustmentReason(adjustmentReason);
        commodities.add(adjustmentsViewModel);
        return new AdjustmentsAdapter(activity, R.layout.selected_adjustment_commodity_list_item, commodities);
    }

    @Test
    public void shouldShowLinearLayoutAllocatedForReceivedFromAnotherFacility() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY);
        LinearLayout linearLayout = (LinearLayout) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.linearLayoutAllocated);
        ANDROID.assertThat(linearLayout).isInvisible();
    }

    @Test
    public void shouldHideLinearLayoutAllocatedForWhenReasonIsNotReceivedFromAnotherFacility() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.PHYSICAL_COUNT);
        LinearLayout linearLayout = (LinearLayout) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.linearLayoutAllocated);
        ANDROID.assertThat(linearLayout).isGone();
    }

    @Test
    public void shouldHaveTextCountedForPhysicalCount() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.PHYSICAL_COUNT);
        TextView textViewCounted = (TextView) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.textViewCounted);
        ANDROID.assertThat(textViewCounted).isVisible();
        ANDROID.assertThat(textViewCounted).hasText(R.string.counted);
    }

    @Test
    public void shouldHaveTextReceivedForReceivedFromAnotherFacility() throws Exception {
        AdjustmentsAdapter adapter = getAdjustmentsAdapter(AdjustmentReason.RECEIVED_FROM_ANOTHER_FACILITY);
        TextView textViewCounted = (TextView) getViewFromListRow(adapter, R.layout.selected_adjustment_commodity_list_item, R.id.textViewCounted);
        ANDROID.assertThat(textViewCounted).isVisible();
        ANDROID.assertThat(textViewCounted).hasText(R.string.received);
    }

}