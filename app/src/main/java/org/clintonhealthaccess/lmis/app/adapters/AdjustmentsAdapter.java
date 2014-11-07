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

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.AdjustmentsActivity;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.AdjustmentsViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.views.NumberTextView;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static java.lang.Math.abs;
import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class AdjustmentsAdapter extends ArrayAdapter<AdjustmentsViewModel> {
    private final int resource;
    private final AdjustmentsActivity activity;

    public AdjustmentsAdapter(AdjustmentsActivity activity, int resource, List<AdjustmentsViewModel> objects) {
        super(activity, resource, objects);
        this.activity = activity;
        this.resource = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);

        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        LinearLayout linearLayoutStockValues = (LinearLayout) rowView.findViewById(R.id.linearLayoutStockValues);
        TextView textViewCurrentStock = (TextView) rowView.findViewById(R.id.textViewCurrentStock);
        TextView textViewMonthsOfStock = (TextView) rowView.findViewById(R.id.textViewMonthsOfStock);
        TextView textViewCounted = (TextView) rowView.findViewById(R.id.textViewCounted);
        Spinner spinnerAdjustmentType = (Spinner) rowView.findViewById(R.id.spinnerAdjustmentType);
        final NumberTextView editTextQuantity = (NumberTextView) rowView.findViewById(R.id.editTextQuantity);

        LinearLayout linearLayoutDifference = (LinearLayout)rowView.findViewById(R.id.linearLayoutDifference);
        TextView textViewAdjustment = (TextView) rowView.findViewById(R.id.textViewAdjustment);

        ImageButton imageButtonCancel = (ImageButton) rowView.findViewById(R.id.imageButtonCancel);

        final AdjustmentsViewModel commodityViewModel = getItem(position);

        final List<String> types = new ArrayList<>();

        if (commodityViewModel.getAdjustmentReason() != null) {
            if (commodityViewModel.getAdjustmentReason().allowsPostive()) {
                types.add("+");
            }
            if (commodityViewModel.getAdjustmentReason().allowsNegative()) {
                types.add("-");
            }
            if (commodityViewModel.getAdjustmentReason().isPhysicalCount()) {
                linearLayoutDifference.setVisibility(View.VISIBLE);
                textViewCounted.setVisibility(View.VISIBLE);
                spinnerAdjustmentType.setVisibility(View.INVISIBLE);
                showAndSetStockValueFields(linearLayoutStockValues, textViewCurrentStock, textViewMonthsOfStock, commodityViewModel);
            } else {
                spinnerAdjustmentType.setVisibility(View.VISIBLE);
                linearLayoutDifference.setVisibility(View.GONE);
                textViewCounted.setVisibility(View.GONE);
                linearLayoutStockValues.setVisibility(View.GONE);
                if (commodityViewModel.getAdjustmentReason().isSentToAnotherFacility()) {
                    showAndSetStockValueFields(linearLayoutStockValues, textViewCurrentStock, textViewMonthsOfStock, commodityViewModel);
                }
            }
            if (types.size() < 2) {
                spinnerAdjustmentType.setEnabled(false);
            } else {
                spinnerAdjustmentType.setEnabled(true);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.simple_spinner_bold, types);
        spinnerAdjustmentType.setAdapter(adapter);
        spinnerAdjustmentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent1, View view, int position1, long id) {
                String selected = types.get(position1);
                commodityViewModel.setPositive(selected.equals("+"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent1) {

            }
        });
        textViewCommodityName.setText(commodityViewModel.getName());
        if (activity.spinnerAdjustmentReason.getSelectedItem().toString().equals(AdjustmentReason.RETURNED_TO_LGA_TEXT)
                && !commodityViewModel.getCommodity().isVaccine()) {
            textViewCommodityName.requestFocus();
            textViewCommodityName.setError(activity.getString(R.string.not_related_to_vaccine));
        }
        editTextQuantity.addTextChangedListener(new AdjustmentQuantityTextWatcher(commodityViewModel, editTextQuantity, textViewAdjustment, spinnerAdjustmentType, types));
        int quantity = commodityViewModel.getQuantityEntered();
        if (quantity > 0) {
            editTextQuantity.setText(Integer.toString(quantity));
        }

        activateCancelButton(imageButtonCancel, commodityViewModel);

        return rowView;
    }

    private void showAndSetStockValueFields(LinearLayout linearLayoutStockValues, TextView textViewCurrentStock, TextView textViewMonthsOfStock, AdjustmentsViewModel commodityViewModel) {
        linearLayoutStockValues.setVisibility(View.VISIBLE);
        textViewCurrentStock.setText("Current Stock:  " + commodityViewModel.getStockOnHand());
        DecimalFormat format = new DecimalFormat("0.00");
        textViewMonthsOfStock.setText("Month Of Stock:  " + format.format(commodityViewModel.getMonthsOfStock()));
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final BaseCommodityViewModel commodityViewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel));
            }
        });
    }

    private static class AdjustmentQuantityTextWatcher extends LmisTextWatcher {
        private final AdjustmentsViewModel adjustmentsViewModel;
        private final EditText editTextQuantity;
        private final TextView textViewAdjustment;
        private final Spinner spinnerAdjustmentType;
        private final List<String> types;

        public AdjustmentQuantityTextWatcher(AdjustmentsViewModel adjustmentsViewModel,
                                             EditText editTextQuantity, TextView textViewAdjustment,
                                             Spinner spinnerAdjustmentType, List<String> types) {
            this.adjustmentsViewModel = adjustmentsViewModel;
            this.editTextQuantity = editTextQuantity;
            this.textViewAdjustment = textViewAdjustment;
            this.spinnerAdjustmentType = spinnerAdjustmentType;
            this.types = types;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String value = editable.toString();
            int quantity = getIntFromString(value);
            adjustmentsViewModel.setQuantityEntered(quantity);
            if (adjustmentsViewModel.getAdjustmentReason().isPhysicalCount()) {
                int adjustment = quantity - adjustmentsViewModel.getStockOnHand();
                boolean isPositive = adjustment > 0;
                adjustmentsViewModel.setPositive(isPositive);
                spinnerAdjustmentType.setSelection(isPositive ? types.indexOf("+") : types.indexOf("-"));
                textViewAdjustment.setText(String.valueOf(adjustment));
                adjustmentsViewModel.setQuantityEntered(adjustment);
            } else {
                if (!adjustmentsViewModel.isPositive()) {
                    int stockOnHand = adjustmentsViewModel.getStockOnHand();
                    if (stockOnHand - quantity < 0) {
                        editTextQuantity.setError(String.format("Can't reduce stock by more than %d", stockOnHand));
                    } else {
                        editTextQuantity.setError(null);
                    }
                }

            }
        }
    }
}
