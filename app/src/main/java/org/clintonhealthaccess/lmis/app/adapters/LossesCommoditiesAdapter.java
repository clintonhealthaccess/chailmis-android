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
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesViewModelCommands;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.List;

import de.greenrobot.event.EventBus;

public class LossesCommoditiesAdapter extends ArrayAdapter<LossesCommodityViewModel> {

    private final int resource;

    public LossesCommoditiesAdapter(Context context, int resource, List<LossesCommodityViewModel> commodities) {
        super(context, resource, commodities);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);
        final LossesCommodityViewModel viewModel = getItem(position);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        textViewCommodityName.setText(viewModel.getName());

        setUpDamages(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextDamages));
        setUpWastages(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextWastages));
        setUpExpiries(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextExpiries));
        setUpMissing(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextMissing));
        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), viewModel);
        return rowView;
    }

    private void setUpWastages(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextWastages) {
        if (viewModel.getWastage() != 0)
            editTextWastages.setText(String.valueOf(viewModel.getWastage()));
        setupTextWatcher(textViewCommodityName, editTextWastages, new LossesViewModelCommands.SetWastageCommand(), viewModel);
    }

    private void setUpDamages(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextDamages) {
        if(viewModel.getDamages() != 0)
            editTextDamages.setText(String.valueOf(viewModel.getDamages()));
        setupTextWatcher(textViewCommodityName, editTextDamages, new LossesViewModelCommands.SetDamagesCommand(), viewModel);
    }

    private void setUpExpiries(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextExpiries) {
        if(viewModel.getExpiries() != 0)
            editTextExpiries.setText(String.valueOf(viewModel.getExpiries()));
        setupTextWatcher(textViewCommodityName, editTextExpiries, new LossesViewModelCommands.SetExpiriesCommand(), viewModel);
    }

    private void setUpMissing(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextMissing) {
        if(viewModel.getMissing() != 0)
            editTextMissing.setText(String.valueOf(viewModel.getMissing()));
        setupTextWatcher(textViewCommodityName, editTextMissing, new LossesViewModelCommands.SetMissingCommand(), viewModel);
    }

    private void setupTextWatcher(final TextView textViewCommodityName, final EditText editText, final LossesViewModelCommands.Command command, final LossesCommodityViewModel viewModel) {
        editText.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                command.execute(viewModel, editable);
                int losses = viewModel.totalLosses();
                int stockOnHand = viewModel.getStockOnHand();
                if (losses > stockOnHand) {
                    textViewCommodityName.setError(String.format(getContext().getString(R.string.totalLossesGreaterThanStockAtHand), losses, stockOnHand));
                } else {
                    textViewCommodityName.setError(null);
                }
            }
        });
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final LossesCommodityViewModel viewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
            }
        });
    }

}
