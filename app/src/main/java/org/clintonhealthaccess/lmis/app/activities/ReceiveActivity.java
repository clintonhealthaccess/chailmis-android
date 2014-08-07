package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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


public class ReceiveActivity extends CommoditySelectableActivity {
    @InjectView(R.id.buttonSubmitReceive)
    Button buttonSubmitReceive;

    @InjectView(R.id.textViewAllocationId)
    AutoCompleteTextView textViewAllocationId;

    @Inject
    AllocationService allocationService;

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

    }

    public Receive generateReceive() {
        Receive receive = new Receive();
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
        return textViewAllocationId.getError() == null;
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
                validateAllocationId(text);
                if (allocationIdIsValid()) {
                    populateWithAllocation(allocationService.getAllocationByLmisId(textViewAllocationId.getText().toString()));
                }
            }
        });
        textViewAllocationId.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = adapter.getItem(position);
                validateAllocationId(text);
                if (allocationIdIsValid()) {
                    populateWithAllocation(allocationService.getAllocationByLmisId(textViewAllocationId.getText().toString()));
                }
            }
        });

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
