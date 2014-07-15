package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;

import java.util.List;

import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class OrderActivity extends CommoditySelectableActivity {
    // FIXME: id need change here
    @Override
    protected int getSelectedCommoditiesAdapterId() {
        return R.layout.commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<CommodityViewModel> selectedCommodities) {}

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    protected boolean allowCheckboxVisibility() {
        return true;
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return ALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }
}
