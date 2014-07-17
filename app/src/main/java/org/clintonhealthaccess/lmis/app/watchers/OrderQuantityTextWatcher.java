package org.clintonhealthaccess.lmis.app.watchers;

import android.text.Editable;
import android.text.TextWatcher;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;

import de.greenrobot.event.EventBus;

public class OrderQuantityTextWatcher implements TextWatcher {
    private CommodityViewModel commodityViewModel;

    public OrderQuantityTextWatcher(CommodityViewModel commodityViewModel1) {
        this.commodityViewModel = commodityViewModel1;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String quantityString = editable.toString();
        int quantityInt = 0;
        try {
            quantityInt = Integer.parseInt(quantityString);
        } catch (NumberFormatException ex) {
            quantityInt = 0;
        }

        EventBus.getDefault().post(new OrderQuantityChangedEvent(quantityInt, commodityViewModel));
    }
}
