package org.clintonhealthaccess.lmis.app.adapters;

import android.app.Dialog;
import android.view.View;
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
import org.robolectric.shadows.ShadowDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SelectedOrderCommoditiesAdapterTest {

    private SelectedOrderCommoditiesAdapter adapter;
    private int list_item_layout = R.layout.selected_order_commodity_list_item;
    private List<OrderReason> orderReasons = new ArrayList<>();
    private OrderReason emergency = new OrderReason("Emergency", OrderReason.ORDER_REASONS_JSON_KEY);
    private OrderReason routine = new OrderReason("Routine", OrderReason.ORDER_REASONS_JSON_KEY);
    private CommodityViewModel commodityViewModel;

    @Before
    public void setUp() {
        setUpInjection(this);
        Commodity commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn("Aspirin");
        when(commodity.getOrderDuration()).thenReturn(30);
        when(commodity.getStockItem()).thenReturn(new StockItem(commodity, 20));

        ArrayList<CommodityViewModel> commodities = new ArrayList<>();
        commodityViewModel = mock(CommodityViewModel.class);
        when(commodityViewModel.getCommodity()).thenReturn(commodity);
        when(commodityViewModel.getOrderReasonPosition()).thenReturn(0);
        commodities.add(commodityViewModel);

        orderReasons.add(routine);
        orderReasons.add(emergency);

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
    public void endDateShouldAlwaysBeGreaterThanStartDateWhenEndDateChanges() throws Exception {
        TextView textViewEndDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewEndDate);
        TextView textViewStartDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewStartDate);
        adapter.spinnerOrderReasons = mock(Spinner.class);
        when(adapter.spinnerOrderReasons.getItemAtPosition(anyInt())).thenReturn("Emergency");
        Date startDate = new Date(2014, 1, 1);
        Date endDate = new Date(2013, 1, 1);

        SimpleDateFormat simpleDateFormat = SelectedOrderCommoditiesAdapter.simpleDateFormat;

        TextView spiedEndDateTextView = spy(textViewEndDate);

        adapter.textViewEndDate = spiedEndDateTextView;

        textViewStartDate.setText(simpleDateFormat.format(startDate));

        spiedEndDateTextView.setText(simpleDateFormat.format(endDate));

        verify(spiedEndDateTextView).setError(Robolectric.application.getString(R.string.end_date_error));

    }

    @Test
    public void endDateShouldAlwaysBeGreaterThanStartDateWhenStartDateChanges() throws Exception {
        TextView textViewStartDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.textViewStartDate);
        adapter.spinnerOrderReasons = mock(Spinner.class);
        when(adapter.spinnerOrderReasons.getItemAtPosition(anyInt())).thenReturn("Emergency");
        Date startDate = new Date(2014, 1, 1);
        Date endDate = new Date(2013, 1, 1);

        SimpleDateFormat simpleDateFormat = SelectedOrderCommoditiesAdapter.simpleDateFormat;

        TextView spiedStartDateTextView = spy(textViewStartDate);

        adapter.textViewStartDate = spiedStartDateTextView;

        adapter.textViewEndDate.setText(simpleDateFormat.format(endDate));

        assertFalse(adapter.textViewEndDate.getText().toString().isEmpty());

        spiedStartDateTextView.setText(simpleDateFormat.format(startDate));

        verify(spiedStartDateTextView).setError(Robolectric.application.getString(R.string.end_date_error));

    }

    @Test
    public void shouldPutOrderReasonsIntoOrderReasonsSpinnerAdapter() {
        Spinner spinner = (Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerOrderReasons);
        String reasonName = (String) spinner.getAdapter().getItem(0);
        assertThat(reasonName, is(routine.getReason()));
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
}