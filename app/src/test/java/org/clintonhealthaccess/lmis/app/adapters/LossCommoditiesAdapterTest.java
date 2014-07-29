package org.clintonhealthaccess.lmis.app.adapters;

import android.widget.EditText;
import android.widget.ImageButton;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class LossCommoditiesAdapterTest {

    private int list_item_layout;
    private LossesCommoditiesAdapter adapter;

    @Before
    public void setUp() {
        Commodity commodity = new Commodity("Commodity");
        List<LossesCommodityViewModel> commodities = Arrays.asList(new LossesCommodityViewModel(commodity));
        list_item_layout = R.layout.losses_commodity_list_item;
        adapter = new LossesCommoditiesAdapter(Robolectric.application, list_item_layout, commodities);
    }

    @Test
    public void shouldSetViewModelParametersOnEditTextInput() {
        enterValues(adapter);

        LossesCommodityViewModel viewModel = adapter.getItem(0);

        assertThat(viewModel.getWastage(), is(10));
        assertThat(viewModel.getMissing(), is(20));
        assertThat(viewModel.getExpiries(), is(30));
        assertThat(viewModel.getDamages(), is(40));
    }

    @Ignore("W.I.P")
    @Test
    public void shouldRemoveItemOnCancelButtonClick() {
        ImageButton cancelButton = (ImageButton)getViewFromListRow(adapter, list_item_layout, R.id.imageButtonCancel);

        assertThat(adapter.getCount(), is(1));

        cancelButton.performClick();

        assertThat(adapter.getCount(), is(0));
    }

    private void enterValues(LossesCommoditiesAdapter adapter) {
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextWastages)).setText("10");
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextMissing)).setText("20");
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextExpiries)).setText("30");
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextDamages)).setText("40");
    }
}