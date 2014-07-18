package org.clintonhealthaccess.lmis.app.watchers;

import android.text.Editable;
import android.text.TextWatcher;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class OrderQuantityTextWatcher implements TextWatcher {
    private final CommodityViewModel commodityViewModel;

    public OrderQuantityTextWatcher(CommodityViewModel commodityViewModel1) {
        this.commodityViewModel = commodityViewModel1;
    }

    private Timer timer = new Timer();
    public long DELAY = 1000;

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(final Editable editable) {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String quantityString = editable.toString();
                int quantityInt = 0;
                try {
                    quantityInt = Integer.parseInt(quantityString);
                } catch (NumberFormatException ex) {
                    quantityInt = 0;
                }
                commodityViewModel.setQuantityEntered(quantityInt);
                EventBus.getDefault().post(new OrderQuantityChangedEvent(quantityInt, commodityViewModel));

            }


        }, DELAY);
    }
}
