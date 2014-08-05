package org.clintonhealthaccess.lmis.app.adapters;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ConfirmLossesAdapterTest {

    public static final String COMMODITY_NAME = "Panado";
    private Commodity commodity;
    private ConfirmLossesAdapter confirmLossesAdapter;
    private ListView parent;

    @Before
    public void setUp() throws Exception {
        parent = new ListView(Robolectric.application);
        commodity = mock(Commodity.class);
        when(commodity.getName()).thenReturn(COMMODITY_NAME);
        when(commodity.getStockOnHand()).thenReturn(20);

        LossItem lossItem = new LossItem();
        lossItem.setCommodity(commodity);
        lossItem.setDamages(2);
        lossItem.setExpiries(3);

        List<LossItem> lossItems = Arrays.asList(lossItem);
        confirmLossesAdapter = new ConfirmLossesAdapter(Robolectric.application, R.layout.losses_confirm_list_item, lossItems);
    }

    @Test
    public void shouldShowTotalLossesOnConfirmLossesDialog() throws Exception {
        View view = this.confirmLossesAdapter.getView(0, null, parent);

        TextView textViewTotalLosses = (TextView) view.findViewById(R.id.totalLosses);
        int totalLosses = getIntFromString(textViewTotalLosses.getText().toString());

        assertThat(totalLosses, is(5));
    }

    @Test
    public void shouldShowNewStockOnHandOnConfirmLossesDialog() throws Exception {
        View view = this.confirmLossesAdapter.getView(0, null, parent);

        String commodityName = ((TextView) view.findViewById(R.id.lossesCommodityName)).getText().toString();

        TextView textViewNewStockOnHand = (TextView) view.findViewById(R.id.newStockOnHand);
        int newStockOnHand = getIntFromString(textViewNewStockOnHand.getText().toString());

        assertThat(commodityName, is(COMMODITY_NAME));
        assertThat(newStockOnHand, is(15));
    }
}