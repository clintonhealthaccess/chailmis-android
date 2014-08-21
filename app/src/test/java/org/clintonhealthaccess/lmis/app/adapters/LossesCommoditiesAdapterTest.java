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