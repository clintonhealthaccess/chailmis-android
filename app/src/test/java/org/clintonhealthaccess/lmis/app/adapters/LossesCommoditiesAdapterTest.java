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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.app.models.LossReason.EXPIRED;
import static org.clintonhealthaccess.lmis.app.models.LossReason.MISSING;
import static org.clintonhealthaccess.lmis.app.models.LossReason.WASTED;
import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestFixture.buildMockCommodity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class LossesCommoditiesAdapterTest {

    boolean toggleEventFired = false;
    private int list_item_layout;
    private LossesCommoditiesAdapter adapter;
    private Commodity mockCommodity;

    @Before
    public void setUp() {
        mockCommodity = buildMockCommodity();
        when(mockCommodity.getStockOnHand()).thenReturn(10000);
        List<LossesCommodityViewModel> commodities = Arrays.asList(new LossesCommodityViewModel(mockCommodity));
        list_item_layout = R.layout.losses_commodity_list_item;
        adapter = new LossesCommoditiesAdapter(Robolectric.application, list_item_layout, commodities);
        EventBus.getDefault().register(this);
    }

    @Test
    public void shouldSetViewModelParametersOnEditTextInput() {
        List<EditText> allInputFields = getAllInputFields(adapter, list_item_layout);
        allInputFields.get(0).setText("10");
        allInputFields.get(1).setText("20");
        allInputFields.get(2).setText("30");

        LossesCommodityViewModel viewModel = adapter.getItem(0);

        assertThat(viewModel.getLoss(EXPIRED), is(10));
        assertThat(viewModel.getLoss(WASTED), is(20));
        assertThat(viewModel.getLoss(MISSING), is(30));
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
        Commodity commodity = buildMockCommodity();
        LossesCommodityViewModel lossesCommodityViewModel = new LossesCommodityViewModel(commodity);
        lossesCommodityViewModel.setLoss(WASTED, 3);
        lossesCommodityViewModel.setLoss(MISSING, 1);
        lossesCommodityViewModel.setLoss(EXPIRED, 4);
        List<LossesCommodityViewModel> commodities = Arrays.asList(lossesCommodityViewModel);
        list_item_layout = R.layout.losses_commodity_list_item;
        adapter = new LossesCommoditiesAdapter(Robolectric.application, list_item_layout, commodities);

        List<EditText> allInputFields = getAllInputFields(adapter, list_item_layout);

        int expiries = getIntFromString(allInputFields.get(0).getText().toString());
        int wastages = getIntFromString(allInputFields.get(1).getText().toString());
        int missing = getIntFromString(allInputFields.get(2).getText().toString());

        assertThat(wastages, is(3));
        assertThat(missing, is(1));
        assertThat(expiries, is(4));
    }

    @Ignore("WIP - Job")
    @Test
    public void shouldSetErrorsOnCommodityIfTotalLossesAreGreaterThanStockOnHand() {
        when(mockCommodity.getStockOnHand()).thenReturn(10);

        List<EditText> allInputFields = getAllInputFields(adapter, list_item_layout);
        allInputFields.get(1).setText("8");

        TextView textViewCommodityName = (TextView)getViewFromListRow(adapter, list_item_layout, R.id.textViewCommodityName);
        assertNull(textViewCommodityName.getError());


        assertThat(textViewCommodityName.getError().toString(), is("Total quantity lost (18) is greater than stock at hand (10)"));

        assertNull(textViewCommodityName.getError());
    }

    public void onEvent(CommodityToggledEvent event) {
        if (!event.getCommodity().isSelected()) {
            toggleEventFired = true;
        }
    }

    private List<EditText> getAllInputFields(ArrayAdapter adapter, int row_layout) {
        List<EditText> results = newArrayList();

        ViewGroup genericLayout = new LinearLayout(Robolectric.application);
        View convertView = LayoutInflater.from(Robolectric.application).inflate(row_layout, null);
        ViewGroup row = (ViewGroup) adapter.getView(0, convertView, genericLayout);
        LinearLayout inputsLinearLayout = (LinearLayout) row.getChildAt(2);

        for (int i = 0; i < inputsLinearLayout.getChildCount(); i++) {
            View childView = inputsLinearLayout.getChildAt(i);
            if (childView instanceof EditText) {
                results.add((EditText) childView);
            }
        }
        return results;
    }
}