package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.ReceiveCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class ReceiveActivity extends CommoditySelectableActivity {
    @InjectView(R.id.buttonSubmitReceive)
    Button buttonSubmitReceive;

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

    }

    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<ReceiveCommodityViewModel> receiveCommodityViewModels = new ArrayList<>();
                for(Commodity commodity: commodities) {
                    receiveCommodityViewModels.add(new ReceiveCommodityViewModel(commodity));
                }
                return receiveCommodityViewModels;
            }
        };
    }


}
