/*
 * Copyright (c) 2014, Clinton Health Access Initiative
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.fragments.DispenseConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.services.DispensingService;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;


public class DispenseActivity extends CommoditySelectableActivity {
    private final QuantityValidator INVALID_AMOUNT = new QuantityValidator(R.string.dispense_submit_validation_message_zero, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            try {
                return Integer.parseInt(editTextQuantity.getText().toString()) <= 0;
            } catch (NumberFormatException ex) {
                return false;
            }

        }
    });
    private final QuantityValidator EMPTY = new QuantityValidator(R.string.dispense_submit_validation_message_filled, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            return editTextQuantity.getText().toString().isEmpty();
        }
    });
    private final QuantityValidator HAS_ERROR = new QuantityValidator(R.string.dispense_submit_validation_message_errors, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            return editTextQuantity.getError() != null;
        }
    });
    @InjectView(R.id.buttonSubmitDispense)
    Button buttonSubmitDispense;

    @InjectView(R.id.checkboxDispenseToFacility)
    CheckBox checkboxDispenseToFacility;

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

    private boolean hasInvalidField(List<QuantityValidator> validators) {
        for (final QuantityValidator validator : validators) {
            if (!validator.isValid()) {
                showToastMessage(validator.toastMessage());
                return true;
            }
        }
        return false;
    }

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
    protected Button getSubmitButton() {
        return buttonSubmitDispense;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitDispense.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (hasInvalidField(of(INVALID_AMOUNT, EMPTY, HAS_ERROR))) {
                            return;
                        }

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        DispenseConfirmationFragment dialog = DispenseConfirmationFragment.newInstance(getDispensing());
                        dialog.show(fragmentManager, "confirmDispensing");
                    }
                }
        );

        checkboxDispenseToFacility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textViewPrescriptionId.setVisibility(View.INVISIBLE);
                    textViewPrescriptionText.setVisibility(View.INVISIBLE);
                } else {
                    textViewPrescriptionId.setVisibility(View.VISIBLE);
                    textViewPrescriptionText.setVisibility(View.VISIBLE);
                }
            }
        });

        textViewPrescriptionId.setText(dispensingService.getNextPrescriptionId());
        checkboxDispenseToFacility.setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean is_adjustments = bundle.getBoolean(HomeActivity.IS_ADJUSTMENT);
            if (is_adjustments) {
                checkboxDispenseToFacility.setVisibility(View.VISIBLE);
                checkboxDispenseToFacility.setChecked(true);
                checkboxDispenseToFacility.setEnabled(false);
                textViewCategories.setBackgroundColor(getResources().getColor(R.color.losses_background));
                textViewPageTitle.setBackgroundColor(getResources().getColor(R.color.losses_background));
                textViewPageTitle.setTextColor(getResources().getColor(R.color.losses_text));
                textViewCategories.setTextColor(getResources().getColor(R.color.losses_text));
                textViewPageTitle.setText(getString(R.string.adjustments));
            }
        }

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
        return R.layout.selected_commodity_list_item;
    }

    Dispensing getDispensing() {
        final Dispensing dispensing = new Dispensing();
        boolean dispenseToFacility = checkboxDispenseToFacility.isChecked();
        dispensing.setDispenseToFacility(dispenseToFacility);
        if (!dispenseToFacility) {
            dispensing.setPrescriptionId(textViewPrescriptionId.getText().toString());
        }
        onEachSelectedCommodity(new SelectedCommodityHandler() {
            @Override
            public void operate(View view, BaseCommodityViewModel commodityViewModel) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                int quantity = getIntFromString(editTextQuantity.getText().toString());
                dispensing.getDispensingItems().add(new DispensingItem(commodityViewModel.getCommodity(), quantity));
            }
        });
        return dispensing;
    }

    private class QuantityValidator {
        private final Predicate<EditText> predicate;
        private int toastMessageStringId;

        public QuantityValidator(int stringId, Predicate<EditText> predicate) {
            toastMessageStringId = stringId;
            this.predicate = predicate;
        }

        private boolean isValid() {
            return filter(wrap(gridViewSelectedCommodities), new Predicate<View>() {
                @Override
                public boolean apply(View view) {
                    EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                    return predicate.apply(editTextQuantity);
                }
            }).isEmpty();
        }

        private List<View> wrap(GridView gridView) {
            List<View> result = newArrayList();
            for (int i = 0; i < gridView.getChildCount(); i++) {
                result.add(gridView.getChildAt(i));
            }
            return result;
        }

        public String toastMessage() {
            return getString(toastMessageStringId);
        }
    }
}
