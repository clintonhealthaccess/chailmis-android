package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.LossesCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class LossesActivity extends CommoditySelectableActivity {

    @InjectView(R.id.buttonSubmitLosses)
    Button buttonSubmitLosses;

    @Override
    protected void onCommoditySelectionChanged(List<BaseCommodityViewModel> selectedCommodities) {

    }

    @Override
    protected Button getSubmitButton() {
        return buttonSubmitLosses;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_losses;
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new LossesCommoditiesAdapter(this, R.layout.losses_commodity_list_item, new ArrayList<LossesCommodityViewModel>());
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        //Do submit stuff here.
    }

    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<BaseCommodityViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    viewModels.add(new LossesCommodityViewModel(commodity));
                }
                return viewModels;
            }
        };
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

}
