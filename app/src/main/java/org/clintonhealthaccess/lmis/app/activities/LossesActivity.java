package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.LossesCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.fragments.LossesConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Loss;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class LossesActivity extends CommoditySelectableActivity {

    @InjectView(R.id.buttonSubmitLosses)
    Button buttonSubmitLosses;

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
        buttonSubmitLosses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    LossesConfirmationFragment lossesConfirmationFragment = LossesConfirmationFragment.newInstance(new Loss());
                    lossesConfirmationFragment.show(supportFragmentManager, "lossesDialog");
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fillInSomeLosses), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isValid() {
        int count = arrayAdapter.getCount();
        for (int i = 0; i < count; i++) {
            LossesCommodityViewModel lossesCommodityViewModel = (LossesCommodityViewModel) arrayAdapter.getItem(i);
            if (!lossesCommodityViewModel.isValid()) {
                return false;
            }
        }
        return true;
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
