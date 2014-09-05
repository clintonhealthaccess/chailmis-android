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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.OrderCycle;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowDatePickerDialog;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.DAY_OF_MONTH;
import static org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter.MIN_ORDER_PERIOD;
import static org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter.SIMPLE_DATE_FORMAT;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SelectedOrderCommoditiesAdapterTest {

    public static final String ROUTINE = "Routine";
    private SelectedOrderCommoditiesAdapter adapter;
    private int list_item_layout = R.layout.selected_order_commodity_list_item;
    private List<OrderReason> orderReasons = new ArrayList<>();
    private OrderReason highDemand = new OrderReason("High Demand");
    private OrderReason adjustments = new OrderReason("Adjustments");
    private OrderReason losses = new OrderReason("losses");
    private OrderReason expiries = new OrderReason("expiries");
    private OrderCommodityViewModel commodityViewModel;
    private ArrayList<OrderCommodityViewModel> commodities;

    OrderType routine = new OrderType(OrderType.ROUTINE);
    OrderType emergency = new OrderType(OrderType.EMERGENCY);

    @Before
    public void setUp() {
        setUpInjection(this);
        Commodity commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn("Aspirin");
        when(commodity.getOrderDuration()).thenReturn(30);
        when(commodity.getStockItem()).thenReturn(new StockItem(commodity, 20));

        commodities = new ArrayList<>();
        commodityViewModel = new OrderCommodityViewModel(commodity, 10);
        commodityViewModel.setOrderReasonPosition(0);
        commodities.add(commodityViewModel);
        orderReasons.add(losses);
        orderReasons.add(highDemand);
        orderReasons.add(adjustments);
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons, routine);
    }

    @Test
    public void shouldShowDateDialogWhenStartDateTextFieldIsClicked() {
        TextView textViewStartDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewStartDate);
        textViewStartDate.performClick();

        Dialog dateDialog = ShadowDialog.getLatestDialog();
        assertNotNull(dateDialog);
    }

    @Test
    public void shouldShowDateDialogWhenEndDateEditTextIsClicked() throws Exception {
        TextView textViewEndDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewEndDate);
        textViewEndDate.performClick();

        Dialog dateDialog = ShadowDialog.getLatestDialog();
        assertNotNull(dateDialog);
    }

    @Test
    public void shouldNotBeAbleToSetTheEndDateEarlierThanStartDate() throws Exception {
        View rowView = getRowView();

        TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);

        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        Calendar calendarStartDate = Calendar.getInstance();

        calendarStartDate.add(DAY_OF_MONTH, 50);

        String startDateAsText = SIMPLE_DATE_FORMAT.format(calendarStartDate.getTime());

        textViewStartDate.setText(startDateAsText);

        assertThat(((TextView) rowView.findViewById(R.id.textViewStartDate)).getText().toString(), is(startDateAsText));

        textViewEndDate.performClick();

        DatePickerDialog dateDialog = (DatePickerDialog) ShadowDatePickerDialog.getLatestDialog();

        calendarStartDate.add(DAY_OF_MONTH, MIN_ORDER_PERIOD);

        Date minDate = new Date(dateDialog.getDatePicker().getMinDate());

        String minEndDateAsText = SIMPLE_DATE_FORMAT.format(minDate);

        startDateAsText = SIMPLE_DATE_FORMAT.format(calendarStartDate.getTime());

        assertThat(minEndDateAsText, is(startDateAsText));
    }

    @Test
    public void shouldNotBeAbleToSetStartDateGreaterThanEndDate() throws Exception {
        View rowView = getRowView();

        TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);

        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        Calendar calendarEndDate = Calendar.getInstance();

        calendarEndDate.add(DAY_OF_MONTH, 30);

        textViewEndDate.setText(SIMPLE_DATE_FORMAT.format(calendarEndDate.getTime()));

        textViewStartDate.performClick();

        DatePickerDialog dateDialog = (DatePickerDialog) ShadowDatePickerDialog.getLatestDialog();

        calendarEndDate.add(DAY_OF_MONTH, -MIN_ORDER_PERIOD);

        Date maxDate = new Date(dateDialog.getDatePicker().getMaxDate());

        String maxStartDateAsText = SIMPLE_DATE_FORMAT.format(maxDate);

        String endDateAsText = SIMPLE_DATE_FORMAT.format(calendarEndDate.getTime());

        assertThat(maxStartDateAsText, is(endDateAsText));

    }


    @Test
    public void shouldShowUnExpectedReasonsSpinnerIfQuantityIsUnexpected() throws Exception {
        View rowView = getRowView();

        EditText editTextOrderQuantity = (EditText) rowView.findViewById(R.id.editTextOrderQuantity);
        Spinner spinnerUnexpectedQuantityReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);

        assertThat(spinnerUnexpectedQuantityReasons, is(notNullValue()));

        ANDROID.assertThat(spinnerUnexpectedQuantityReasons).isNotVisible();

        editTextOrderQuantity.setText("20");

        ANDROID.assertThat(spinnerUnexpectedQuantityReasons).isVisible();

        editTextOrderQuantity.setText("2");

        ANDROID.assertThat(spinnerUnexpectedQuantityReasons).isNotVisible();
    }

    @Test
    public void shouldSetEndDateGivenStartDateAndTheOrderTypeIsRoutine() throws Exception {
        View rowView = getRowView();
        TextView startDate = (TextView) rowView.findViewById(R.id.textViewStartDate);
        startDate.setText("01-Jan-14");
        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);
        String endDate = textViewEndDate.getText().toString();
        Date expectedEndDate = OrderCycle.Monthly.endDate(new Date());
        assertThat(endDate, is(SelectedOrderCommoditiesAdapter.SIMPLE_DATE_FORMAT.format(expectedEndDate)));
    }

    @Test
    public void shouldDisableEndDateWhenOrderTypeIsRoutine() throws Exception {
        View rowView = getRowView();
        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);
        ANDROID.assertThat(textViewEndDate).isDisabled();
    }


    @Test
    public void shouldShowSpinnerForUnexpectedOrderReasonsIfStartOrderDateIsChangedWhenOrderTypeIsRoutine() throws Exception {
        Date currentDate = new Date();
        commodityViewModel.setOrderReasonPosition(null);
        commodityViewModel.setOrderPeriodStartDate(currentDate);
        View rowView = getRowView();

        TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        textViewStartDate.setText(SelectedOrderCommoditiesAdapter.SIMPLE_DATE_FORMAT.format(calendar.getTime()));
        Spinner spinnerUnexpectedOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);

        ANDROID.assertThat(spinnerUnexpectedOrderReasons).isVisible();
    }

    @Test
    public void shouldShowSpinnerForUnexpectedOrderReasonsIfEndOrderDateIsChangedWhenOrderReasonIsRoutine() throws Exception {
        Date currentDate = new Date();
        commodityViewModel.setOrderReasonPosition(null);
        commodityViewModel.setOrderPeriodEndDate(currentDate);
        View rowView = getRowView();

        TextView textView = (TextView) rowView.findViewById(R.id.textViewEndDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        textView.setText(SelectedOrderCommoditiesAdapter.SIMPLE_DATE_FORMAT.format(calendar.getTime()));
        Spinner spinnerUnexpectedOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);

        ANDROID.assertThat(spinnerUnexpectedOrderReasons).isVisible();
    }

    @Test
    public void shouldShowSpinnerForUnExpectedOrderReasonsIfOrderTypeIsNotRoutine() throws Exception {
        commodityViewModel.setExpectedOrderQuantity(10);
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons, emergency);
        View rowView = getRowView();
        Spinner spinnerUnexpectedOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        ANDROID.assertThat(spinnerUnexpectedOrderReasons).isVisible();
    }

    @Test
    public void shouldHideSpinnerForUnExpectedOrderReasonsIfOrderTypeIsRoutine() throws Exception {
        commodityViewModel.setExpectedOrderQuantity(10);
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons, routine);
        View rowView = getRowView();
        Spinner spinnerUnexpectedOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        ANDROID.assertThat(spinnerUnexpectedOrderReasons).isNotVisible();
    }

    @Test
    public void shouldDefaultToBlankForUnExpectedReasons() throws Exception {
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons, routine);
        View rowView = getRowView();


        Spinner spinnerUnexpectedOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);

        assertThat(((OrderReason) spinnerUnexpectedOrderReasons.getSelectedItem()).getReason(), is(Robolectric.application.getString(R.string.select_reason)));
    }

    @Test
    public void shouldPrePopulateOrderPeriodStartAndEndDateWhenOrderReasonIsRoutine() throws Exception {
        Date currentDate = new Date();
        commodities = new ArrayList<>();
        commodityViewModel.setOrderPeriodStartDate(currentDate);
        commodities.add(commodityViewModel);
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons, routine);
        View rowView = getRowView();

        TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);
        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        String dateString = SIMPLE_DATE_FORMAT.format(currentDate);
        String endDateString = SIMPLE_DATE_FORMAT.format(commodityViewModel.getExpectedEndDate());
        ANDROID.assertThat(textViewStartDate).hasText(dateString);
        assertThat(textViewEndDate.getText().toString(), is(endDateString));

    }

    private View getRowView() {
        ViewGroup genericLayout = getLinearLayout();
        View convertView = LayoutInflater.from(Robolectric.application).inflate(list_item_layout, null);
        return adapter.getView(0, convertView, genericLayout);
    }

    private LinearLayout getLinearLayout() {
        return new LinearLayout(Robolectric.application);
    }
}