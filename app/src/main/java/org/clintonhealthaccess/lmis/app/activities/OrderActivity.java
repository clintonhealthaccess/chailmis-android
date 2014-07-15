package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.CommoditiesAdapter;

import java.util.List;

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

    @Override
    protected boolean allowCheckboxVisibility() {
        return true;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }
}
