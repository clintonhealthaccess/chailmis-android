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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.DispenseActivity;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Arrays;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class SelectedCommoditiesAdapterTest {
    private DispenseActivity dispenseActivity;
    private SelectedCommoditiesAdapter adapter;
    private StockService stockServiceMock;

    @Before
    public void setUp() throws Exception {
        stockServiceMock = mock(StockService.class);
        when(stockServiceMock.getStockLevelFor((Commodity) anyObject())).thenReturn(10);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(stockServiceMock);
            }
        });
        dispenseActivity = setupActivity(DispenseActivity.class);
        adapter = new SelectedCommoditiesAdapter(dispenseActivity, R.layout.selected_commodity_list_item, new ArrayList<BaseCommodityViewModel>());
    }

    @Test
    public void shouldShowKeyboardWhenTextIsEnteredIntoQuantityField() throws Exception {
        Commodity commodity = new Commodity("food");
        BaseCommodityViewModel viewModel = new BaseCommodityViewModel(commodity);
        adapter = new SelectedCommoditiesAdapter(dispenseActivity, R.layout.selected_commodity_list_item, Arrays.asList(viewModel));
        View rowView = getRowView();
        ANDROID.assertThat(dispenseActivity.keyBoardView).isNotShown();
        EditText editText = (EditText) rowView.findViewById(R.id.editTextQuantity);
        editText.performClick();
        ANDROID.assertThat(dispenseActivity.keyBoardView).isShown();
    }

    private View getRowView() {
        ViewGroup genericLayout = getLinearLayout();
        View convertView = LayoutInflater.from(Robolectric.application).inflate(R.layout.selected_commodity_list_item, null);
        return adapter.getView(0, convertView, genericLayout);
    }

    private LinearLayout getLinearLayout() {
        return new LinearLayout(dispenseActivity);
    }
}