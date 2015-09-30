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
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.ReceiveCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.AllocationCreateEvent;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.ReceiveConfirmFragment;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.services.AllocationService;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roboguice.inject.InjectView;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class ReceiveActivity extends CommoditySelectableActivity implements Serializable {
    public static final String ALLOCATION_ID = "ALLOCATION_ID";

    @InjectView(R.id.buttonSubmitReceive)
    Button buttonSubmitReceive;

    @InjectView(R.id.textViewAllocationId)
    AutoCompleteTextView textViewAllocationId;


    @InjectView(R.id.spinnerSource)
    public Spinner spinnerSource;

    @InjectView(R.id.textViewAllocationLabel)
    TextView textViewAllocationLabel;

    @Inject
    AllocationService allocationService;

    Allocation allocation;

    private List<String> completedAllocationIds;
    private String presetAllocationId;


    @Override
    protected Button getSubmitButton() {
        return buttonSubmitReceive;
    }

    @Override
    protected String getActivityName() {
        return "Receive";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_receive;
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return new CommodityDisplayStrategy() {
            @Override
            public boolean allowClick(BaseCommodityViewModel commodityViewModel) {
                String selectedSource = (String) spinnerSource.getSelectedItem();
                if (selectedSource.equals(getString(R.string.zonal_store_for_receive))) {
                    return commodityViewModel.getCommodity().isNonLGA();
                } else {
                    if (selectedSource.equals(getString(R.string.lga_for_receive))) {
                        if (allocation == null) {
                            return false;
                        }
                    }
                    if ("Vaccines".equals(commodityViewModel.getCommodity().getCategory().getName())) {
                        return false;
                    } else {
                        return !commodityViewModel.getCommodity().isNonLGA();
                    }
                }
            }

            @Override
            public String getMessage() {
                return String.format("Can't Be Received from %s", spinnerSource.getSelectedItem().toString());
            }

            @Override
            public String getEmptyMessage() {
                String currentSource = spinnerSource.getSelectedItem().toString();
                if (currentSource.equals(getString(R.string.zonal_store_for_receive))) {
                    return String.format("Commodities in this category can not be received from %s", currentSource);
                } else if (currentSource.equals(getString(R.string.others_for_receive))) {
                    return "Select source: zonal cold chain store";
                } else {
                    return "Please enter valid allocation ID";
                }
            }

            @Override
            public boolean hideCommodities() {
                return true;
            }
        };
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new ReceiveCommoditiesAdapter(this, R.layout.selected_receive_commodity_list_item, new ArrayList<ReceiveCommodityViewModel>());
    }

    @Override
    protected AdapterView.OnItemClickListener getAutoCompleteTextViewCommoditiesAdapterListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Commodity commodity = searchCommodityAdapter.getItem(position);
                onEvent(new CommodityToggledEvent(new ReceiveCommodityViewModel(commodity)));
                autoCompleteTextViewCommodities.setText("");
            }
        };
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        completedAllocationIds = new ArrayList<>(allocationService.getReceivedAllocationIds());
        setupAllocationIdTextView();
        setupReceiveButton();

        if (presetAllocationId != null) {
            textViewAllocationId.setText(presetAllocationId);
        }
    }

    public List<String> getReceiveSources() {
        return Arrays.asList(getString(R.string.others_for_receive), getString(R.string.zonal_store_for_receive), getString(R.string.lga_for_receive));
    }

    @Override
    protected void beforeSetUpCommoditySearch() {
        Intent intent = getIntent();
        String allocationId = intent.getStringExtra(ALLOCATION_ID);

        if(StringUtils.isNotBlank(allocationId)) {
            Allocation allocationWithId = allocationService.getAllocationByLmisId(allocationId);
            if (allocationWithId != null && !allocationWithId.isReceived()) {
                presetAllocationId = allocationId;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getReceiveSources());
        spinnerSource.setAdapter(adapter);
        spinnerSource.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        String selected = getReceiveSources().get(position);
                        if (selected.contains(getString(R.string.lga_for_receive))) {
                            setAllocation(textViewAllocationId.getText().toString());
                            textViewAllocationId.setEnabled(true);
                            textViewAllocationId.setVisibility(View.VISIBLE);
                            textViewAllocationLabel.setVisibility(View.VISIBLE);

                        } else {
                            allocation = null;
                            textViewAllocationId.setError(null);
                            textViewAllocationId.setVisibility(View.INVISIBLE);
                            textViewAllocationLabel.setVisibility(View.INVISIBLE);
                            textViewAllocationId.setEnabled(false);
                        }
                        List<CommodityToggledEvent> events = new ArrayList<CommodityToggledEvent>();
                        if (selected.contains(getString(R.string.zonal_store_for_receive))) {
                            for (BaseCommodityViewModel model : selectedCommodities) {
                                if (!model.getCommodity().isNonLGA()) {
                                    events.add(new CommodityToggledEvent(model));
                                }
                            }
                        } else {
                            for (BaseCommodityViewModel model : selectedCommodities) {
                                if (model.getCommodity().isNonLGA()) {
                                    events.add(new CommodityToggledEvent(model));
                                }
                            }
                        }

                        for (CommodityToggledEvent event : events) {
                            onEvent(event);
                        }

                        ((ReceiveCommoditiesAdapter)arrayAdapter).setQuantityAllocatedDisplay(!selected.contains(getString(R.string.others_for_receive))
                                && !selected.contains(getString(R.string.zonal_store_for_receive)));
                        arrayAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );

    }

    private void setupReceiveButton() {
        buttonSubmitReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allocationIdIsValid(textViewAllocationId.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.receive_submit_validation_message_allocation_id), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!quantitiesAreValid()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.receive_quantities_validation_error_message), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (completedAllocationIds.contains(textViewAllocationId.getText().toString())){
                    Toast.makeText(getApplicationContext(), getString(R.string.error_allocation_received), Toast.LENGTH_SHORT).show();
                    return;
                }


                ReceiveConfirmFragment receiveConfirmFragment = ReceiveConfirmFragment.newInstance(generateReceive());
                receiveConfirmFragment.setQuantityAllocatedDisplay(((ReceiveCommoditiesAdapter)arrayAdapter).isQuantityAllocatedDisplay());
                receiveConfirmFragment.show(getSupportFragmentManager(), "receiveDialog");
            }
        });
    }

    public Receive generateReceive() {
        //user input a allocation id, getEndPoint a dummy one
        if ((allocation == null && spinnerSource.getSelectedItem().toString().contains(getString(R.string.lga_for_receive)))
                || (allocation != null && allocation.isDummy())) {
            generateDummyAllocation(textViewAllocationId.getText().toString().trim());
        }

        Receive receive = new Receive(spinnerSource.getSelectedItem().toString(), allocation);
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            ReceiveCommodityViewModel viewModel = (ReceiveCommodityViewModel) arrayAdapter.getItem(i);
            ReceiveItem receiveItem = viewModel.getReceiveItem();
            receive.addReceiveItem(receiveItem);
        }
        return receive;
    }

    private void generateDummyAllocation(String allocationId) {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        allocation = new Allocation(allocationId, today);
        allocation.setDummy(true);
        allocation.setReceived(true);
        allocation.setTransientAllocationItems(new ArrayList<AllocationItem>());
        Log.i("ReceiveActivity", "Create a Dummy allocation ....");
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

    private void setupAllocationIdTextView() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, allocationService.getYetToBeReceivedAllocationIds());
        textViewAllocationId.setAdapter(adapter);

        textViewAllocationId.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.trim().length() >= 6) {
                    setAllocation(text);
                }
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

    public void setAllocation(String allocationId) {
        if (!allocationId.trim().isEmpty()) {
            validateAllocationId(allocationId);
        }
        if (allocationIdIsValid(allocationId) || presetAllocationId != null) {
            allocation = allocationService.getAllocationByLmisId(textViewAllocationId.getText().toString());
            populateWithAllocation(allocation);
        } else {
            allocation = null;
        }
    }

    private void populateWithAllocation(Allocation allocation) {
        if (allocation != null) {
            selectedCommodities.clear();
            arrayAdapter.clear();
            for (AllocationItem item : allocation.getAllocationItems()) {
                CommodityToggledEvent event = new CommodityToggledEvent(new ReceiveCommodityViewModel(item));
                onEvent(event);
            }
        }

    }

    public void validateAllocationId(String text) {
        if (!allocationIdIsValid(text)) {
            textViewAllocationId.setError(String.format(
                    getString(R.string.error_allocation_id_wrong_format), getAllocationIdFormat()));
        } else {
            if (completedAllocationIds.contains(text)) {
                textViewAllocationId.setError(getString(R.string.error_allocation_received));
            } else {
                textViewAllocationId.setError(null);
            }
        }
    }

    public void validateAllocationId() {
        validateAllocationId(textViewAllocationId.getText().toString());
    }

    protected String getAllocationIdFormat() {
        return facility2LetterCode + "0000";
    }

    private boolean allocationIdIsValid(String text) {
        if (!textViewAllocationId.isEnabled()) {
            // allocation != null && text.equals(allocation.getAllocationIds()))
            return true;
        }
        String patternString = facility2LetterCode.toUpperCase() + "\\d+$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    public void onEvent(AllocationCreateEvent event){
        completedAllocationIds.add(event.allocation.getAllocationId());
        Log.i("ReceiveActivity", "completedIds: " + completedAllocationIds.size());
    }

    public void clearInput() {
        clearAllSelectedItems();
        if (textViewAllocationId != null && textViewAllocationId.isEnabled()) {
            textViewAllocationId.setText("");
        }
    }
}
