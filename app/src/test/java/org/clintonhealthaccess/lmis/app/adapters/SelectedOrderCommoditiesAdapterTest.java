package org.clintonhealthaccess.lmis.app.adapters;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter.MIN_DIFFERENCE_BETWEEN_START_END;
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

    private SelectedOrderCommoditiesAdapter adapter;
    private int list_item_layout = R.layout.selected_order_commodity_list_item;
    private List<OrderReason> orderReasons = new ArrayList<>();
    private OrderReason emergency = new OrderReason("Emergency", OrderReason.ORDER_REASONS_JSON_KEY);
    private OrderReason routine = new OrderReason("Routine", OrderReason.ORDER_REASONS_JSON_KEY);
    private CommodityViewModel commodityViewModel;
    private ArrayList<CommodityViewModel> commodities;

    @Before
    public void setUp() {
        setUpInjection(this);
        Commodity commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn("Aspirin");
        when(commodity.getOrderDuration()).thenReturn(30);
        when(commodity.getStockItem()).thenReturn(new StockItem(commodity, 20));

        commodities = new ArrayList<>();
        commodityViewModel = mock(CommodityViewModel.class);
        when(commodityViewModel.getCommodity()).thenReturn(commodity);
        when(commodityViewModel.getOrderReasonPosition()).thenReturn(0);
        commodities.add(commodityViewModel);

        orderReasons.add(emergency);
        orderReasons.add(routine);

        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons);
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
        ViewGroup genericLayout = new LinearLayout(Robolectric.application);

        View convertView = LayoutInflater.from(Robolectric.application).inflate(list_item_layout, null);

        View rowView = adapter.getView(0, convertView, genericLayout);

        TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);

        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        Calendar calendarStartDate = Calendar.getInstance();

        calendarStartDate.add(DAY_OF_MONTH, 50);

        String startDateAsText = SIMPLE_DATE_FORMAT.format(calendarStartDate.getTime());

        textViewStartDate.setText(startDateAsText);

        assertThat(((TextView) rowView.findViewById(R.id.textViewStartDate)).getText().toString(), is(startDateAsText));

        textViewEndDate.performClick();

        DatePickerDialog dateDialog = (DatePickerDialog) ShadowDatePickerDialog.getLatestDialog();

        calendarStartDate.add(DAY_OF_MONTH, MIN_DIFFERENCE_BETWEEN_START_END);

        Date minDate = new Date(dateDialog.getDatePicker().getMinDate());

        String minEndDateAsText = SIMPLE_DATE_FORMAT.format(minDate);

        startDateAsText = SIMPLE_DATE_FORMAT.format(calendarStartDate.getTime());

        assertThat(minEndDateAsText, is(startDateAsText));
    }

    @Test
    public void shouldNotBeAbleToSetStartDateGreaterThanEndDate() throws Exception {
        ViewGroup genericLayout = new LinearLayout(Robolectric.application);

        View convertView = LayoutInflater.from(Robolectric.application).inflate(list_item_layout, null);

        View rowView = adapter.getView(0, convertView, genericLayout);

        TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);

        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        Calendar calendarEndDate = Calendar.getInstance();

        calendarEndDate.add(DAY_OF_MONTH, 30);

        textViewEndDate.setText(SIMPLE_DATE_FORMAT.format(calendarEndDate.getTime()));

        textViewStartDate.performClick();

        DatePickerDialog dateDialog = (DatePickerDialog) ShadowDatePickerDialog.getLatestDialog();

        calendarEndDate.add(DAY_OF_MONTH, -MIN_DIFFERENCE_BETWEEN_START_END);

        Date maxDate = new Date(dateDialog.getDatePicker().getMaxDate());

        String maxStartDateAsText = SIMPLE_DATE_FORMAT.format(maxDate);

        String endDateAsText = SIMPLE_DATE_FORMAT.format(calendarEndDate.getTime());

        assertThat(maxStartDateAsText, is(endDateAsText));

    }


    @Test
    public void shouldPutOrderReasonsIntoOrderReasonsSpinnerAdapter() {
        Spinner spinner = (Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerOrderReasons);
        String reasonName = ((OrderReason) spinner.getAdapter().getItem(0)).getReason();
        assertThat(reasonName, is(emergency.getReason()));
    }

    @Ignore("Work in Progress")
    @Test
    public void shouldShowUnExpectedReasonsSpinnerIfDataIsUnexpected() throws Exception {


        ArrayList<CommodityViewModel> commodities = new ArrayList<>();
        commodities.add(new CommodityViewModel(new Commodity("food")));
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons);

        Spinner spinnerUnexpectedQuantityReasons = (Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerUnexpectedQuantityReasons);

        assertThat(spinnerUnexpectedQuantityReasons, is(notNullValue()));

        CommodityViewModel mockViewModel = mock(CommodityViewModel.class);
        when(mockViewModel.quantityIsUnexpected()).thenReturn(true);
        adapter.onEventMainThread(new OrderQuantityChangedEvent(12, mockViewModel));

        assertThat(spinnerUnexpectedQuantityReasons.getVisibility(), is(View.VISIBLE));

        CommodityViewModel otherMock = mock(CommodityViewModel.class);
        when(otherMock.quantityIsUnexpected()).thenReturn(false);
        adapter.onEventMainThread(new OrderQuantityChangedEvent(12, otherMock));

        assertThat(spinnerUnexpectedQuantityReasons.getVisibility(), is(View.INVISIBLE));

    }

    @Ignore("Failing to select item in spinner")
    @Test
    public void shouldSetEndDateGivenStartDateAndTheOrderReasonIsRoutine() throws Exception {
        ((TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewStartDate)).setText("10-10-2013");
        Spinner spinner = (Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerOrderReasons);
        ((Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerOrderReasons)).setSelection(0);

        Robolectric.shadowOf(spinner).performItemClick(0);
        String endDate = ((TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewEndDate)).getText().toString();
        assertThat(endDate, is("31-01-2013"));
    }

    @Test
    public void shouldDisableEndDateWhenOrderReasonIsRoutine() throws Exception {
        orderReasons = new ArrayList<>();
        orderReasons.add(routine);
        orderReasons.add(emergency);

        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons);

        ViewGroup genericLayout = new LinearLayout(Robolectric.application);

        View convertView = LayoutInflater.from(Robolectric.application).inflate(list_item_layout, null);

        View rowView = adapter.getView(0, convertView, genericLayout);

        Spinner spinnerOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerOrderReasons);

        spinnerOrderReasons.setSelection(0);

        TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        assertThat(textViewEndDate.isEnabled(), is(false));


    }
}