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
import android.widget.ListView;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.StockItem;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ConfirmDispenseAdapterTest {
    private ConfirmDispenseAdapter adapter;
    private ListView parent;
    private String commodityName;
    private Commodity commodity;

    @Before
    public void setUp() throws Exception {
        parent = new ListView(Robolectric.application);
        commodityName = "food";

        commodity = mock(Commodity.class);

        when(commodity.getName()).thenReturn(commodityName);
        when(commodity.getStockOnHand()).thenReturn(20);
    }

    @Test
    public void testQuantityAdjustedIsVisibleWhenDispensingToFacility() throws Exception {
        ArrayList<DispensingItem> items = new ArrayList<>();
        DispensingItem dispensingItem = new DispensingItem(commodity, 5);
        items.add(dispensingItem);
        Dispensing dispensing = new Dispensing();
        dispensing.setDispenseToFacility(true);

        this.adapter = new ConfirmDispenseAdapter(Robolectric.application, R.layout.confirm_commodity_list_item, items, dispensing);
        View view = this.adapter.getView(0, null, parent);

        TextView name = (TextView) view.findViewById(R.id.textViewCommodityName);
        TextView adjustment = (TextView) view.findViewById(R.id.textViewAdjustedQuantity);
        assertThat(name.getText().toString(), containsString(commodityName));
        assertThat(adjustment.getVisibility(), is(View.VISIBLE));
    }


    @Test
    public void testQuantityAdjustedIsINVisibleWhenNotDispensingToFacility() throws Exception {
        ArrayList<DispensingItem> items = new ArrayList<>();
        DispensingItem dispensingItem = new DispensingItem(commodity, 5);
        items.add(dispensingItem);
        Dispensing dispensing = new Dispensing();
        dispensing.setDispenseToFacility(false);

        this.adapter = new ConfirmDispenseAdapter(Robolectric.application, R.layout.confirm_commodity_list_item, items, dispensing);
        View view = this.adapter.getView(0, null, parent);

        TextView name = (TextView) view.findViewById(R.id.textViewCommodityName);
        TextView adjustment = (TextView) view.findViewById(R.id.textViewAdjustedQuantity);
        assertThat(name.getText().toString(), containsString(commodityName));
        assertThat(adjustment.getVisibility(), is(View.INVISIBLE));
    }


    @Test
    public void testNewStockAtHandWhenDispensingToFacility() throws Exception {
        ArrayList<DispensingItem> items = new ArrayList<>();
        int quantity = 5;
        DispensingItem dispensingItem = new DispensingItem(commodity, quantity);
        items.add(dispensingItem);
        Dispensing dispensing = new Dispensing();
        dispensing.setDispenseToFacility(true);

        this.adapter = new ConfirmDispenseAdapter(Robolectric.application, R.layout.confirm_commodity_list_item, items, dispensing);
        View view = this.adapter.getView(0, null, parent);
        TextView adjustment = (TextView) view.findViewById(R.id.textViewAdjustedQuantity);
        TextView soh = (TextView) view.findViewById(R.id.textViewSOH);
        assertThat(getIntFromString(soh.getText().toString()), is(15));
        assertThat(getIntFromString(adjustment.getText().toString()), is(quantity));

    }

    @Test
    public void testQuantityWhenNotDispensingToFacility() throws Exception {
        ArrayList<DispensingItem> items = new ArrayList<>();
        int quantity = 5;
        DispensingItem dispensingItem = new DispensingItem(commodity, quantity);
        items.add(dispensingItem);
        Dispensing dispensing = new Dispensing();
        dispensing.setDispenseToFacility(false);

        this.adapter = new ConfirmDispenseAdapter(Robolectric.application, R.layout.confirm_commodity_list_item, items, dispensing);
        View view = this.adapter.getView(0, null, parent);

        TextView soh = (TextView) view.findViewById(R.id.textViewSOH);
        assertThat(getIntFromString(soh.getText().toString()), is(quantity));

    }


}