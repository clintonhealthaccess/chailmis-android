package org.clintonhealthaccess.lmis.app.watchers;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class OrderQuantityTextWatcher implements TextWatcher {
    private final CommodityViewModel commodityViewModel;
    private final Context context;

    public OrderQuantityTextWatcher(Context activity, CommodityViewModel commodityViewModel1) {
        this.commodityViewModel = commodityViewModel1;
        this.context = activity;
    }

    private Timer timer = new Timer();
    private final long DELAY = 500;

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
                ((Activity) context).runOnUiThread(new Runnable() {

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


                });

            }


        }, DELAY);
    }
}
