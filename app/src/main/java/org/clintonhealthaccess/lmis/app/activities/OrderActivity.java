package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.List;

public class OrderActivity extends CommoditySelectableActivity {
    // FIXME: id need change here
    @Override
    protected int getSelectedCommoditiesAdapterId() {
        return R.layout.commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<Commodity> selectedCommodities) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }
}
