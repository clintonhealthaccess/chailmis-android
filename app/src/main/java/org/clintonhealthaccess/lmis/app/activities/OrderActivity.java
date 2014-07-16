package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.services.OrderService;

import java.util.ArrayList;
import java.util.List;

import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class OrderActivity extends CommoditySelectableActivity {

    @Inject
    OrderService orderService;

    // FIXME: id need change here
    @Override
    protected int getSelectedCommoditiesAdapterId() {
        return R.layout.selected_order_commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<CommodityViewModel> selectedCommodities) {}

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
//        orderService.
        return new SelectedOrderCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<CommodityViewModel>(), null);
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return ALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }
}
