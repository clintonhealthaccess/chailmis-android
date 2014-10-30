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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.AdjustmentsViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.AdjustmentsAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.ConfirmAdjustmentsFragment;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.AdjustmentService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;

public class AdjustmentsActivity extends CommoditySelectableActivity {
    public static final String ADJUSTMENT_REASON = "adjustment_reason";
    protected final ViewValidator<Spinner> HAS_NO_TYPE = new ViewValidator<Spinner>(R.string.adjustment_type_validation_message, new Predicate<Spinner>() {
        @Override
        public boolean apply(Spinner view) {
            return view.getChildCount() < 1;
        }
    }, R.id.spinnerAdjustmentType);

    protected final ViewValidator<TextView> TEXT_VIEW_HAS_ERROR =
            new ViewValidator<>(R.string.not_a_vaccine, new Predicate<TextView>() {
                @Override
                public boolean apply(TextView input) {
                    return input.getError() != null;
                }
            }, R.id.textViewCommodityName);

    @InjectView(R.id.spinnerAdjustmentReason)
    public Spinner spinnerAdjustmentReason;

    @InjectView(R.id.buttonSubmitAdjustments)
    Button buttonSubmitAdjustments;

    @Inject
    CommodityService commodityService;

    @Override
    public Button getSubmitButton() {
        return buttonSubmitAdjustments;
    }

    @Override
    protected String getActivityName() {
        return "Adjustments";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_adjustments;
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return new CommodityDisplayStrategy() {

            @Override
            public boolean allowClick(BaseCommodityViewModel commodityViewModel) {
                if (spinnerAdjustmentReason.getSelectedItem().toString().equals(AdjustmentReason.RETURNED_TO_LGA_TEXT)) {
                    return commodityViewModel.getCommodity().getCategory().getName().toLowerCase().contains("vaccine");
                } else {
                    return true;
                }
            }

            @Override
            public String getMessage() {
                return "Not vaccine device";
            }

            @Override
            public String getEmptyMessage() {
                return "Commodities in this category can not be returned to LGA";
            }

            @Override
            public boolean hideCommodities() {
                return true;
            }

        };
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new AdjustmentsAdapter(AdjustmentsActivity.this, R.layout.selected_adjustment_list_item, new ArrayList<AdjustmentsViewModel>());
    }

    @Override
    protected AdapterView.OnItemClickListener getAutoCompleteTextViewCommoditiesAdapterListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Commodity commodity = searchCommodityAdapter.getItem(position);
                AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity);
                adjustmentsViewModel.setAdjustmentReason((AdjustmentReason) spinnerAdjustmentReason.getSelectedItem());
                onEvent(new CommodityToggledEvent(adjustmentsViewModel));
                autoCompleteTextViewCommodities.setText("");
            }
        };
    }

    @Override
    protected void beforeSetUpCommoditySearch() {
        setUpSpinnerAdjustmentReason();
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitAdjustments.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (hasInvalidEditTextField(of(EMPTY, HAS_ERROR))) {
                            return;
                        }

                        if (hasInvalidSpinnerField(of(HAS_NO_TYPE))) {
                            return;
                        }

                        if (hasInvalidTextViewField(of(TEXT_VIEW_HAS_ERROR))) {
                            return;
                        }

                        List<Adjustment> adjustments = getAdjustments();

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        ConfirmAdjustmentsFragment dialog = ConfirmAdjustmentsFragment.newInstance(
                                new ArrayList<>(adjustments));
                        dialog.show(fragmentManager, "confirmAdjustments");
                    }
                }
        );
    }

    private List<Adjustment> getAdjustments() {
        return FluentIterable.from(selectedCommodities).transform(new Function<BaseCommodityViewModel, Adjustment>() {
            @Override
            public Adjustment apply(BaseCommodityViewModel input) {
                AdjustmentsViewModel model = (AdjustmentsViewModel) input;

                return new Adjustment(model.getCommodity(), model.getQuantityEntered(), model.isPositive(), model.getAdjustmentReason().getName());
            }
        }).toList();
    }

    private void setUpSpinnerAdjustmentReason() {
        Intent intent = getIntent();
        String presetReason = intent.getStringExtra(ADJUSTMENT_REASON);

        final ArrayList<AdjustmentReason> adjustmentReasons = AdjustmentService.getAdjustmentReasons();

        ArrayAdapter<AdjustmentReason> adapter = new ArrayAdapter<AdjustmentReason>(AdjustmentsActivity.this, R.layout.simple_spinner_bold, adjustmentReasons);
        spinnerAdjustmentReason.setAdapter(adapter);
        if (presetReason != null) {
            int selectedIndex = adjustmentReasons.indexOf(new AdjustmentReason(presetReason, true, true));
            spinnerAdjustmentReason.setSelection(selectedIndex);
            for (Commodity commodity : commodityService.all()) {
                AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity, commodity.getStockOnHand());
                adjustmentsViewModel.setAdjustmentReason(AdjustmentReason.PHYSICAL_COUNT);
                onEvent(new CommodityToggledEvent(adjustmentsViewModel));
            }
        }

        spinnerAdjustmentReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AdjustmentReason selectedReason = adjustmentReasons.get(position);
                for (BaseCommodityViewModel model : selectedCommodities) {
                    AdjustmentsViewModel adjustmentsViewModel = (AdjustmentsViewModel) model;
                    adjustmentsViewModel.setAdjustmentReason(selectedReason);
                    adjustmentsViewModel.setQuantityEntered(0);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<AdjustmentsViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    AdjustmentsViewModel adjustmentsViewModel = new AdjustmentsViewModel(commodity);
                    adjustmentsViewModel.setAdjustmentReason((AdjustmentReason) spinnerAdjustmentReason.getSelectedItem());
                    viewModels.add(adjustmentsViewModel);
                }
                return viewModels;
            }
        };
    }

    private boolean hasInvalidTextViewField(List<ViewValidator<TextView>> validators) {
        for (final ViewValidator validator : validators) {
            if (!validator.isValid()) {
                showToastMessage(validator.toastMessage());
                return true;
            }
        }
        return false;
    }
}
