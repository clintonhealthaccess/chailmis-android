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
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.ReceiveCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.ReceiveConfirmFragment;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.services.AllocationService;
import org.clintonhealthaccess.lmis.app.validators.AllocationIdValidator;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class ReceiveActivity extends CommoditySelectableActivity {
    @InjectView(R.id.buttonSubmitReceive)
    Button buttonSubmitReceive;

    @InjectView(R.id.textViewAllocationId)
    AutoCompleteTextView textViewAllocationId;

    @InjectView(R.id.checkBoxReceiveFromFacility)
    public CheckBox checkBoxReceiveFromFacility;

    @Inject
    AllocationService allocationService;

    Allocation allocation;

    private List<String> completedAllocationIds;


    @Override
    protected Button getSubmitButton() {
        return buttonSubmitReceive;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_receive;
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new ReceiveCommoditiesAdapter(this, R.layout.receive_commodity_list_item, new ArrayList<ReceiveCommodityViewModel>());
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        completedAllocationIds = allocationService.getReceivedAllocationIds();
        setupAllocationIdTextView();
        buttonSubmitReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allocationIdIsValid()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.receive_submit_validation_message_allocation_id), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!quantitiesAreValid()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.receive_quantities_validation_error_message), Toast.LENGTH_LONG).show();
                    return;
                }
                ReceiveConfirmFragment receiveConfirmFragment = ReceiveConfirmFragment.newInstance(generateReceive());
                receiveConfirmFragment.show(getSupportFragmentManager(), "receiveDialog");
            }
        });

        checkBoxReceiveFromFacility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                textViewAllocationId.setEnabled(!isChecked);
                if (isChecked)
                    allocation = null;
            }
        });

    }

    public Receive generateReceive() {
        Receive receive = new Receive(checkBoxReceiveFromFacility.isChecked(), allocation);
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            ReceiveCommodityViewModel viewModel = (ReceiveCommodityViewModel) arrayAdapter.getItem(i);
            ReceiveItem receiveItem = viewModel.getReceiveItem();
            receive.addReceiveItem(receiveItem);
        }
        return receive;
    }

    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<ReceiveCommodityViewModel> receiveCommodityViewModels = new ArrayList<>();
                for (Commodity commodity : commodities) {
                    receiveCommodityViewModels.add(new ReceiveCommodityViewModel(commodity));
                }
                return receiveCommodityViewModels;
            }
        };
    }

    private boolean quantitiesAreValid() {
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            ReceiveCommodityViewModel viewModel = (ReceiveCommodityViewModel) arrayAdapter.getItem(i);
            if (viewModel.getQuantityAllocated() == 0 && viewModel.getQuantityReceived() == 0)
                return false;
        }
        return true;
    }

    private boolean allocationIdIsValid() {
        return !textViewAllocationId.isEnabled() || (textViewAllocationId.getError() == null && !isBlank(textViewAllocationId.getText().toString()));
    }

    private void setupAllocationIdTextView() {
        textViewAllocationId.setValidator(new AllocationIdValidator());

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, allocationService.getYetToBeReceivedAllocationIds());
        textViewAllocationId.setAdapter(adapter);

        textViewAllocationId.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                setAllocation(text);
            }
        });
        textViewAllocationId.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = adapter.getItem(position);
                setAllocation(text);
            }
        });
    }

    private void setAllocation(String allocationId) {
        validateAllocationId(allocationId);
        if (allocationIdIsValid()) {
            allocation = allocationService.getAllocationByLmisId(textViewAllocationId.getText().toString());
            populateWithAllocation(allocation);
        } else {
            allocation = null;
        }
    }

    void populateWithAllocation(Allocation allocation) {
        if (allocation != null) {
            selectedCommodities.clear();
            arrayAdapter.clear();
            for (AllocationItem item : allocation.getAllocationItems()) {
                CommodityToggledEvent event = new CommodityToggledEvent(new ReceiveCommodityViewModel(item));
                onEvent(event);
            }
        }

    }

    private void validateAllocationId(String text) {
        if (!textViewAllocationId.getValidator().isValid(text)) {
            textViewAllocationId.setError(getString(R.string.error_allocation_id_wrong_format));
        } else {
            if (completedAllocationIds.contains(text)) {
                textViewAllocationId.setError(getString(R.string.error_allocation_received));
            } else {
                textViewAllocationId.setError(null);
            }
        }
    }

}
