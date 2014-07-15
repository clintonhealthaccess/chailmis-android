package org.clintonhealthaccess.lmis.app.adapters.strategies;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;

import java.io.Serializable;

abstract public class CommodityDisplayStrategy implements Serializable {
    public void apply(CommodityViewModel commodityViewModel, CheckBox checkboxCommoditySelected, TextView alternativeText) {
        if (!allowClick(commodityViewModel)) {
            checkboxCommoditySelected.setVisibility(View.INVISIBLE);
            alternativeText.setVisibility(View.VISIBLE);
        }
    }

    abstract public boolean allowClick(CommodityViewModel commodityViewModel);

    public static final CommodityDisplayStrategy DISALLOW_CLICK_WHEN_OUT_OF_STOCK = new CommodityDisplayStrategy() {
        @Override
        public boolean allowClick(CommodityViewModel commodityViewModel) {
            return !commodityViewModel.stockIsFinished();
        }
    };

    public static final CommodityDisplayStrategy ALLOW_CLICK_WHEN_OUT_OF_STOCK = new CommodityDisplayStrategy() {
        @Override
        public boolean allowClick(CommodityViewModel commodityViewModel) {
            return true;
        }
    };
}
