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

package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.DispenseConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.services.DispensingService;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;


public class DispenseActivity extends CommoditySelectableActivity {
    @InjectView(R.id.buttonSubmitDispense)
    Button buttonSubmitDispense;

    @InjectView(R.id.textViewPrescriptionId)
    TextView textViewPrescriptionId;

    @InjectView(R.id.textViewPrescriptionText)
    TextView textViewPrescriptionText;

    @InjectView(R.id.textViewCategories)
    TextView textViewCategories;

    @InjectView(R.id.textViewPageTitle)
    TextView textViewPageTitle;

    @Inject
    DispensingService dispensingService;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dispense;
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    protected ArrayAdapter getArrayAdapter() {
        return new SelectedCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<BaseCommodityViewModel>());
    }

    @Override
    protected AdapterView.OnItemClickListener getAutoCompleteTextViewCommoditiesAdapterListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Commodity commodity = searchCommodityAdapter.getItem(position);
                onEvent(new CommodityToggledEvent(new BaseCommodityViewModel(commodity)));
                autoCompleteTextViewCommodities.setText("");
            }
        };
    }

    @Override
    protected Button getSubmitButton() {
        return buttonSubmitDispense;
    }

    @Override
    protected String getActivityName() {
        return "Dispense";
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitDispense.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (hasInvalidEditTextField(of(INVALID_AMOUNT, EMPTY, HAS_ERROR))) {
                            return;
                        }

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        DispenseConfirmationFragment dialog = DispenseConfirmationFragment.newInstance(getDispensing());
                        dialog.show(fragmentManager, "confirmDispensing");
                    }
                }
        );

        textViewPrescriptionId.setText(dispensingService.getNextPrescriptionId());
    }

    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<BaseCommodityViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    viewModels.add(new BaseCommodityViewModel(commodity));
                }
                return viewModels;
            }
        };
    }

    private int getSelectedCommoditiesAdapterId() {
        return R.layout.selected_dispense_commodity_list_item;
    }

    Dispensing getDispensing() {
        final Dispensing dispensing = new Dispensing();
        dispensing.setPrescriptionId(textViewPrescriptionId.getText().toString());

        onEachSelectedCommodity(new SelectedCommodityHandler() {
            @Override
            public void operate(View view, BaseCommodityViewModel commodityViewModel) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                int quantity = getIntFromString(editTextQuantity.getText().toString());
                dispensing.addItem(new DispensingItem(commodityViewModel.getCommodity(), quantity));
            }
        });
        return dispensing;
    }


}
