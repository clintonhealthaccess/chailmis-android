package org.clintonhealthaccess.lmis.app.adapters;

import android.app.Dialog;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;

import static junit.framework.Assert.assertNull;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getRowFromListView;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SelectedOrderCommoditiesAdapterTest {

    private SelectedOrderCommoditiesAdapter adapter;

    @Before
    public void setUp() {
        setUpInjection(this);
        Commodity commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn("Asprine");
        when(commodity.getOrderDuration()).thenReturn(30);
        when(commodity.getStockItem()).thenReturn(new StockItem(commodity, 20));

        ArrayList<CommodityViewModel> commodities = new ArrayList<>();
        commodities.add(new CommodityViewModel(commodity));
        adapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, R.layout.selected_order_commodity_list_item, commodities);
    }

    @Test
    public void shouldShowDateDialogWhenStartDateTextFieldIsClicked(){
        TextView textViewStartDate = (TextView)getViewFromListRow(adapter, R.layout.selected_order_commodity_list_item, R.id.editTextStartDate);
        textViewStartDate.performClick();

        Dialog dateDialog = ShadowDialog.getLatestDialog();
        assertNotNull(dateDialog);
    }

    @Test
    public void shouldShowDateDialogWhenEndDateEditTextIsClicked() throws Exception {
        TextView textViewEndDate = (TextView)getViewFromListRow(adapter, R.layout.selected_order_commodity_list_item, R.id.editTextEndDate);
        textViewEndDate.performClick();

        Dialog dateDialog = ShadowDialog.getLatestDialog();
        assertNotNull(dateDialog);
    }
}