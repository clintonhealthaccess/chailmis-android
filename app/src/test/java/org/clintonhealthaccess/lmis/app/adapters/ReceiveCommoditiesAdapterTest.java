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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveCommoditiesAdapterTest {

    public static final String PANADOL = "Panadol";
    public static final int QUANTITY_ORDERED = 4;
    public static final int QUANTITY_RECEIVED = 3;
    public static final int QUANTITY_DIFFERENCE = -1;
    private ListView parent;
    private ReceiveCommoditiesAdapter adapter;

    @Before
    public void setUp() throws Exception {
        parent = new ListView(Robolectric.application);
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL), QUANTITY_ORDERED, QUANTITY_RECEIVED);
        List<ReceiveCommodityViewModel> receiveCommodityViewModels = Arrays.asList(viewModel);
        adapter = new ReceiveCommoditiesAdapter(Robolectric.application, R.layout.selected_receive_commodity_list_item, receiveCommodityViewModels);
    }

    @Test
    public void shouldShowReceiveCommodityFields() throws Exception {
        View rowView = adapter.getView(0, null, parent);
        String commodityName = ((TextView) rowView.findViewById(R.id.textViewCommodityName)).getText().toString();
        int difference = getIntFromString(((TextView) rowView.findViewById(R.id.textViewDifferenceQuantity)).getText().toString());
        int quantityOrdered = getIntFromString(((EditText) rowView.findViewById(R.id.editTextAllocatedQuantity)).getText().toString());
        int quantityReceived = getIntFromString(((EditText) rowView.findViewById(R.id.editTextReceivedQuantity)).getText().toString());

        assertThat(commodityName, is(PANADOL));
        assertThat(quantityOrdered, is(QUANTITY_ORDERED));
        assertThat(quantityReceived, is(QUANTITY_RECEIVED));
        assertThat(difference, is(QUANTITY_DIFFERENCE));
    }

    @Test
    public void shouldUpdateDifferenceTextViewWhenQuantityOrderedAndQuantityReceivedChanges() {
        View rowView = adapter.getView(0, null, parent);
        EditText editTextQuantityOrdered = (EditText) rowView.findViewById(R.id.editTextAllocatedQuantity);
        EditText editTextQuantityReceived = (EditText) rowView.findViewById(R.id.editTextReceivedQuantity);

        editTextQuantityOrdered.setText("7");
        int difference = getIntFromString(((TextView) rowView.findViewById(R.id.textViewDifferenceQuantity)).getText().toString());
        assertThat(difference, is(-4));

        editTextQuantityReceived.setText("2");
        difference = getIntFromString(((TextView) rowView.findViewById(R.id.textViewDifferenceQuantity)).getText().toString());
        assertThat(difference, is(-5));
    }

    @Test
    public void shouldUpdateQuantitiesInViewModelWhenReceivedAndOrderedQuantitiesChange() throws Exception {
        View rowView = adapter.getView(0, null, parent);
        EditText editTextQuantityAllocated = (EditText) rowView.findViewById(R.id.editTextAllocatedQuantity);
        EditText editTextQuantityReceived = (EditText) rowView.findViewById(R.id.editTextReceivedQuantity);

        editTextQuantityAllocated.setText("7");
        ReceiveCommodityViewModel viewModel = adapter.getItem(0);
        assertThat(viewModel.getQuantityAllocated(), is(7));

        editTextQuantityReceived.setText("2");
        viewModel = adapter.getItem(0);
        assertThat(viewModel.getQuantityReceived(), is(2));

    }
}