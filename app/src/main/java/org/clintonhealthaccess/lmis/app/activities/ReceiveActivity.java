package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.ReceiveCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;
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
    ReceiveService receiveService;
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
        completedAllocationIds = receiveService.getCompletedIds();
        textViewAllocationId.setValidator(new AllocationIdValidator());

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, receiveService.getReadyAllocationIds());
        textViewAllocationId.setAdapter(adapter);

        textViewAllocationId.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                validateAllocationId(text);
            }
        });
        textViewAllocationId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = adapter.getItem(position);
                validateAllocationId(text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

}
