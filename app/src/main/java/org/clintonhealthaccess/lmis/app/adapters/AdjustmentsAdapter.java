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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.AdjustmentsViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class AdjustmentsAdapter extends ArrayAdapter<AdjustmentsViewModel> {
    private final int resource;

    public AdjustmentsAdapter(Context context, int resource, List<AdjustmentsViewModel> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        ImageButton imageButtonCancel = (ImageButton) rowView.findViewById(R.id.imageButtonCancel);
        final EditText editTextQuantity = (EditText) rowView.findViewById(R.id.editTextQuantity);
        Spinner spinnerAdjustmentType = (Spinner) rowView.findViewById(R.id.spinnerAdjustmentType);
        final AdjustmentsViewModel commodityViewModel = getItem(position);
        setupAdjustmentType(spinnerAdjustmentType, commodityViewModel);
        textViewCommodityName.setText(commodityViewModel.getName());
        setupQuantity(editTextQuantity, commodityViewModel);
        activateCancelButton(imageButtonCancel, commodityViewModel);
        return rowView;
    }

    private void setupAdjustmentType(Spinner spinnerAdjustmentType, final AdjustmentsViewModel commodityViewModel) {

        final List<String> types = new ArrayList<>();

        if (commodityViewModel.getAdjustmentReason() != null) {
            if (commodityViewModel.getAdjustmentReason().allowsPostive()) {
                types.add("+");
            }
            if (commodityViewModel.getAdjustmentReason().allowsNegative()) {
                types.add("-");
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
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = types.get(position);
                commodityViewModel.setPositive(selected.equals("+"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupQuantity(EditText editTextQuantity, BaseCommodityViewModel commodityViewModel) {
        editTextQuantity.addTextChangedListener(new AdjustmentTextWatcher((AdjustmentsViewModel) commodityViewModel, editTextQuantity));
        int quantity = commodityViewModel.getQuantityEntered();
        if (quantity >= 0) {
            editTextQuantity.setText(Integer.toString(quantity));
        }
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final BaseCommodityViewModel commodityViewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel));
            }
        });
    }

    private static class AdjustmentTextWatcher implements TextWatcher {
        private final AdjustmentsViewModel commodityViewModel;
        private final EditText editText;

        public AdjustmentTextWatcher(AdjustmentsViewModel commodityViewModel, EditText editText) {
            this.commodityViewModel = commodityViewModel;
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String value = editable.toString();
            if (!value.isEmpty()) {
                int quantity = getIntFromString(value);
                commodityViewModel.setQuantityEntered(quantity);
                if (!commodityViewModel.isPositive()) {
                    int stockOnHand = commodityViewModel.getStockOnHand();
                    int result = stockOnHand - quantity;
                    if (result < 0) {
                        editText.setError(String.format("Can't reduce stock by more than %d", stockOnHand));
                    }
                }
            }
        }
    }
}
