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
import android.widget.Toast;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.LossesCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.LossesConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Loss;

import java.util.List;

import roboguice.inject.InjectView;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class LossesActivity extends CommoditySelectableActivity {

    @InjectView(R.id.buttonSubmitLosses)
    Button buttonSubmitLosses;

    @Override
    protected Button getSubmitButton() {
        return buttonSubmitLosses;
    }

    @Override
    protected String getActivityName() {
        return "Losses";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_losses;
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new LossesCommoditiesAdapter(this, R.layout.losses_commodity_list_item);
    }

    @Override
    protected AdapterView.OnItemClickListener getAutoCompleteTextViewCommoditiesAdapterListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Commodity commodity = searchCommodityAdapter.getItem(position);
                onEvent(new CommodityToggledEvent(new LossesCommodityViewModel(commodity)));
                autoCompleteTextViewCommodities.setText("");
            }
        };
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitLosses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    LossesConfirmationFragment lossesConfirmationFragment = LossesConfirmationFragment.newInstance(generateLoss());
                    lossesConfirmationFragment.show(supportFragmentManager, "lossesDialog");
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fillInSomeLosses), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Loss generateLoss() {
        Loss loss = new Loss();
        int count = arrayAdapter.getCount();
        for (int i = 0; i < count; i++) {
            LossesCommodityViewModel lossesCommodityViewModel = (LossesCommodityViewModel) arrayAdapter.getItem(i);
            loss.addLossItem(lossesCommodityViewModel.getLossItem());
        }
        return loss;
    }

    private boolean isValid() {
        int count = arrayAdapter.getCount();
        for (int i = 0; i < count; i++) {
            LossesCommodityViewModel lossesCommodityViewModel = (LossesCommodityViewModel) arrayAdapter.getItem(i);
            if (!lossesCommodityViewModel.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<BaseCommodityViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    viewModels.add(new LossesCommodityViewModel(commodity));
                }
                return viewModels;
            }
        };
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

}
