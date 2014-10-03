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

import android.app.ActionBar;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesViewModelCommand;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.LossReason;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LossesCommoditiesAdapter extends ArrayAdapter<LossesCommodityViewModel> {
    private final int resource;

    public LossesCommoditiesAdapter(Context context, int resource, List<LossesCommodityViewModel> commodities) {
        super(context, resource, commodities);
        this.resource = resource;
    }

    public LossesCommoditiesAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<LossesCommodityViewModel>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);
        final LossesCommodityViewModel viewModel = getItem(position);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        textViewCommodityName.setText(viewModel.getName());

        int i = 0;
        RelativeLayout relativeLayoutContainer = (RelativeLayout) rowView.findViewById(R.id.relativeLayoutContainer);
        LinearLayout linearLayout = addLinearLayout(relativeLayoutContainer, 1, textViewCommodityName.getId(), R.id.imageButtonCancel);
        for (LossReason lossReason : viewModel.getLossReasons()) {
            EditText editText = createLossAmountEditText(linearLayout, lossReason);
            setUpLosses(textViewCommodityName, viewModel, editText, lossReason);
            i++;
            if (i % 3 == 0) {
                linearLayout = addLinearLayout(relativeLayoutContainer, i/3+1, linearLayout.getId(), R.id.imageButtonCancel);
            }
        }

        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), viewModel);
        return rowView;
    }

    private LinearLayout addLinearLayout(RelativeLayout container, int idNo, int belowId, int leftOfId) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, belowId);
        layoutParams.addRule(RelativeLayout.LEFT_OF, leftOfId);

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(10, 10, 10, 10);
        setId(linearLayout, idNo);
        container.addView(linearLayout, layoutParams);
        return linearLayout;
    }

    private void setId(View view, int idNo){
        if(idNo==1)
            view.setId(R.id.dynamicLinearLayout1);
        if(idNo==2)
            view.setId(R.id.dynamicLinearLayout2);
        if(idNo==3)
            view.setId(R.id.dynamicLinearLayout3);
    }

    private EditText createLossAmountEditText(LinearLayout lossAmountsLayout, LossReason lossReason) {
        TextView label = new TextView(getContext());
        label.setText(lossReason.getLabel());
        label.setTextSize(COMPLEX_UNIT_SP, 16);

        EditText editText = new EditText(getContext());
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
        editText.setLayoutParams(layoutParams);
        editText.setTextSize(COMPLEX_UNIT_SP, 16);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        lossAmountsLayout.addView(label);
        lossAmountsLayout.addView(editText);
        return editText;
    }

    private void setUpLosses(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editText, LossReason lossReason) {
        if (viewModel.getLoss(lossReason) != 0) {
            editText.setText(String.valueOf(viewModel.getLoss(lossReason)));
        }
        setupTextWatcher(textViewCommodityName, editText, new LossesViewModelCommand(lossReason), viewModel);
    }

    private void setupTextWatcher(final TextView textViewCommodityName, final EditText editText, final LossesViewModelCommand command, final LossesCommodityViewModel viewModel) {
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
