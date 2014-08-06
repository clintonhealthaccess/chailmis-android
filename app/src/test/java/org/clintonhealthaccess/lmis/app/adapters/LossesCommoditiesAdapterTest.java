package org.clintonhealthaccess.lmis.app.adapters;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class LossesCommoditiesAdapterTest {

    boolean toggleEventFired = false;
    private int list_item_layout;
    private LossesCommoditiesAdapter adapter;
    private Commodity mockCommodity;

    @Before
    public void setUp() {
        mockCommodity = mock(Commodity.class);
        when(mockCommodity.getStockOnHand()).thenReturn(10000);
        List<LossesCommodityViewModel> commodities = Arrays.asList(new LossesCommodityViewModel(mockCommodity));
        list_item_layout = R.layout.losses_commodity_list_item;
        adapter = new LossesCommoditiesAdapter(Robolectric.application, list_item_layout, commodities);
        EventBus.getDefault().register(this);
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

    @Test
    public void shouldToggleItemOnCancelButtonClick() {
        ImageButton cancelButton = (ImageButton) getViewFromListRow(adapter, list_item_layout, R.id.imageButtonCancel);
        assertThat(adapter.getCount(), is(1));

        cancelButton.performClick();
        assertTrue(toggleEventFired);
    }

    @Test
    public void shouldPreLoadEditTextsWithValuesInViewModels() {
        Commodity commodity = new Commodity("Commodity");
        LossesCommodityViewModel lossesCommodityViewModel = new LossesCommodityViewModel(commodity);
        lossesCommodityViewModel.setMissing(1);
        lossesCommodityViewModel.setDamages(2);
        lossesCommodityViewModel.setWastages(3);
        lossesCommodityViewModel.setExpiries(4);
        List<LossesCommodityViewModel> commodities = Arrays.asList(lossesCommodityViewModel);
        list_item_layout = R.layout.losses_commodity_list_item;
        adapter = new LossesCommoditiesAdapter(Robolectric.application, list_item_layout, commodities);

        int missing = getIntFromString(((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextMissing)).getText().toString());
        int damages = getIntFromString(((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextDamages)).getText().toString());
        int wastages = getIntFromString(((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextWastages)).getText().toString());
        int expiries = getIntFromString(((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextExpiries)).getText().toString());

        assertThat(missing, is(1));
        assertThat(damages, is(2));
        assertThat(wastages, is(3));
        assertThat(expiries, is(4));
    }

    @Ignore("WIP - Job")
    @Test
    public void shouldSetErrorsOnCommodityIfTotalLossesAreGreaterThanStockOnHand() {
        when(mockCommodity.getStockOnHand()).thenReturn(10);

        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextMissing)).setText("8");

        TextView textViewCommodityName = (TextView)getViewFromListRow(adapter, list_item_layout, R.id.textViewCommodityName);
        assertNull(textViewCommodityName.getError());

        EditText editTextDamages = (EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextDamages);
        editTextDamages.setText("10");

        assertThat(textViewCommodityName.getError().toString(), is("Total quantity lost (18) is greater than stock at hand (10)"));

        editTextDamages.setText("1");
        assertNull(textViewCommodityName.getError());
    }

    public void onEvent(CommodityToggledEvent event) {
        if (!event.getCommodity().isSelected()) {
            toggleEventFired = true;
        }
    }

    private void enterValues(LossesCommoditiesAdapter adapter) {
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextWastages)).setText("10");
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextMissing)).setText("20");
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextExpiries)).setText("30");
        ((EditText) getViewFromListRow(adapter, list_item_layout, R.id.editTextDamages)).setText("40");
    }
}