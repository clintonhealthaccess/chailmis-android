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

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getRowFromListView;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CommoditiesAdapterTest {


    @Before
    public void setUp() {
        setUpInjection(this);
    }

    @Test
    public void shouldPopulateTextViewWithCommodityName() {
        ArrayList<BaseCommodityViewModel> commodities = new ArrayList<>();
        String commodityName = "game";
        Commodity commodity = new Commodity(commodityName);
        commodities.add(new BaseCommodityViewModel(commodity));
        CommoditiesAdapter adapter = new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities, DISALLOW_CLICK_WHEN_OUT_OF_STOCK);

        TextView textViewCommodityName = (TextView) getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.textViewCommodityName);
        assertThat(textViewCommodityName.getText().toString(), is(commodityName));
    }

    @Test
    public void shouldCheckTheCheckBoxIfCommodityIsSelected() {
        CommoditiesAdapter adapter = makeAdapterWithCommodities();

        CheckBox checkboxCommoditySelectedOne = (CheckBox) getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        CheckBox checkboxCommoditySelectedTwo = (CheckBox) getViewFromListRow(1, adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);

        assertTrue(checkboxCommoditySelectedOne.isChecked());
        assertFalse(checkboxCommoditySelectedTwo.isChecked());
    }

    @Test
    public void shouldMarkCommoditiesWhoseStockIsZeroGivenCheckboxVisibilityStrategyIs_DO_HIDE() {
        CommoditiesAdapter adapter = makeAdapterWithOutOfStockCommodities(CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK);
        View listRow = getRowFromListView(adapter, R.layout.commodity_list_item);

        View checkBox = getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        assertFalse(checkBox.isShown());

        View textView = getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.textViewCommodityOutOfStock);
        assertThat(textView.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void shouldNotMarkCommoditiesWhoseStockIsZeroGivenCheckboxVisibilityStrategyIs_DO_NOTHING() {
        CommoditiesAdapter adapter = makeAdapterWithOutOfStockCommodities(CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK);
        View listRow = getRowFromListView(adapter, R.layout.commodity_list_item);

        View checkBox = getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.checkboxCommoditySelected);
        assertThat(checkBox.getVisibility(), is(View.VISIBLE));

        View textView = getViewFromListRow(adapter, R.layout.commodity_list_item, R.id.textViewCommodityOutOfStock);
        assertThat(textView.getVisibility(), is(View.GONE));
    }

    private CommoditiesAdapter makeAdapterWithCommodities() {
        ArrayList<BaseCommodityViewModel> commodities = new ArrayList<>();
        BaseCommodityViewModel commodityOne = new BaseCommodityViewModel(new Commodity("game"));
        commodityOne.toggleSelected();
        commodities.add(commodityOne);

        commodities.add(new BaseCommodityViewModel(new Commodity("other game")));

        return new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities, DISALLOW_CLICK_WHEN_OUT_OF_STOCK);
    }

    private CommoditiesAdapter makeAdapterWithOutOfStockCommodities(CommodityDisplayStrategy commodityDisplayStrategy) {
        ArrayList<BaseCommodityViewModel> commodities = new ArrayList<>();
        BaseCommodityViewModel commodity = mock(BaseCommodityViewModel.class);
        when(commodity.stockIsFinished()).thenReturn(true);
        commodities.add(commodity);

        return new CommoditiesAdapter(Robolectric.application, R.layout.commodity_list_item, commodities, commodityDisplayStrategy);
    }
}
