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

import java.util.ArrayList;
import java.util.List;

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
    ;

    @Before
    public void setUp() {
        setUpInjection(this);
        Commodity commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn("Aspirin");
        when(commodity.getOrderDuration()).thenReturn(30);
        when(commodity.getStockItem()).thenReturn(new StockItem(commodity, 20));

        ArrayList<CommodityViewModel> commodities = new ArrayList<>();
        commodities.add(new CommodityViewModel(commodity));

        orderReasons.add(routine);
        orderReasons.add(emergency);

        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, list_item_layout, commodities, orderReasons);
    }

    @Test
    public void shouldShowDateDialogWhenStartDateTextFieldIsClicked() {
        TextView textViewStartDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.editTextStartDate);
        textViewStartDate.performClick();

        Dialog dateDialog = ShadowDialog.getLatestDialog();
        assertNotNull(dateDialog);
    }

    @Test
    public void shouldShowDateDialogWhenEndDateEditTextIsClicked() throws Exception {
        TextView textViewEndDate = (TextView) getViewFromListRow(adapter, list_item_layout, R.id.editTextEndDate);
        textViewEndDate.performClick();

        Dialog dateDialog = ShadowDialog.getLatestDialog();
        assertNotNull(dateDialog);
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
        adapter.onEvent(new OrderQuantityChangedEvent(12, mockViewModel));

        assertThat(spinnerUnexpectedQuantityReasons.getVisibility(), is(View.VISIBLE));

        CommodityViewModel otherMock = mock(CommodityViewModel.class);
        when(otherMock.quantityIsUnexpected()).thenReturn(false);
        adapter.onEvent(new OrderQuantityChangedEvent(12, otherMock));

        assertThat(spinnerUnexpectedQuantityReasons.getVisibility(), is(View.INVISIBLE));

    }

    @Ignore("Failing to select item in spinner")
    @Test
    public void shouldSetEndDateGivenStartDateAndTheOrderReasonIsRoutine() throws Exception {
        ((TextView) getViewFromListRow(adapter, list_item_layout, R.id.editTextStartDate)).setText("10-10-2013");
        Spinner spinner = (Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerOrderReasons);
        ((Spinner) getViewFromListRow(adapter, list_item_layout, R.id.spinnerOrderReasons)).setSelection(0);

        Robolectric.shadowOf(spinner).performItemClick(0);
        String endDate = ((TextView) getViewFromListRow(adapter, list_item_layout, R.id.editTextEndDate)).getText().toString();
        assertThat(endDate, is("31-01-2013"));
    }
}