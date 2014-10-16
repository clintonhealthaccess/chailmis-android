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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.views.NumberTextView;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.List;

import de.greenrobot.event.EventBus;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class ReceiveCommoditiesAdapter extends ArrayAdapter<ReceiveCommodityViewModel> {

    private int resource;

    public ReceiveCommoditiesAdapter(Context context, int resource, List<ReceiveCommodityViewModel> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, parent, false);

        holder.textViewCommodityName = (TextView) convertView.findViewById(R.id.textViewCommodityName);
        holder.imageButtonCancel = (ImageButton) convertView.findViewById(R.id.imageButtonCancel);
        holder.editTextAllocatedQuantity = (NumberTextView) convertView.findViewById(R.id.editTextAllocatedQuantity);
        holder.editTextReceivedQuantity = (NumberTextView) convertView.findViewById(R.id.editTextReceivedQuantity);
        holder.textViewDifferenceQuantity = (TextView) convertView.findViewById(R.id.textViewDifferenceQuantity);


        ReceiveCommodityViewModel viewModel = getItem(position);
        initialiseQuantities(holder, viewModel);
        activateCancelButton(holder.imageButtonCancel, viewModel);

        setupTextWatchers(holder, viewModel);
        return convertView;
    }

    private void initialiseQuantities(ViewHolder holder, ReceiveCommodityViewModel viewModel) {
        holder.textViewCommodityName.setText(viewModel.getCommodity().getName());

        if (viewModel.getQuantityAllocated() != 0) {
            holder.editTextAllocatedQuantity.setText(String.valueOf(viewModel.getQuantityAllocated()));
        }
        if (viewModel.getQuantityReceived() != 0) {
            holder.editTextReceivedQuantity.setText(String.valueOf(viewModel.getQuantityReceived()));
        }

        holder.textViewDifferenceQuantity.setText(String.valueOf(viewModel.getDifference()));
        holder.editTextAllocatedQuantity.setEnabled(!viewModel.isQuantityAllocatedDisabled());
    }

    private void setupTextWatchers(final ViewHolder viewHolder, final ReceiveCommodityViewModel receiveCommodityViewModel) {
        viewHolder.editTextAllocatedQuantity.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                receiveCommodityViewModel.setQuantityAllocated(getIntFromString(s.toString()));
                viewHolder.textViewDifferenceQuantity.setText(String.valueOf(receiveCommodityViewModel.getDifference()));
            }
        });

        viewHolder.editTextReceivedQuantity.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                receiveCommodityViewModel.setQuantityReceived(getIntFromString(s.toString()));
                viewHolder.textViewDifferenceQuantity.setText(String.valueOf(receiveCommodityViewModel.getDifference()));
            }
        });
    }

    static class ViewHolder {
        TextView textViewCommodityName;
        ImageButton imageButtonCancel;
        EditText editTextAllocatedQuantity;
        EditText editTextReceivedQuantity;
        TextView textViewDifferenceQuantity;
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final ReceiveCommodityViewModel viewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
            }
        });
    }
}
