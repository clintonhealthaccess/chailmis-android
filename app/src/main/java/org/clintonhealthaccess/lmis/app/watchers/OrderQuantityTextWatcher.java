package org.clintonhealthaccess.lmis.app.watchers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class OrderQuantityTextWatcher implements TextWatcher {
    private final CommodityViewModel commodityViewModel;
    private final EditText editTextQuantity;
    private final Context context;
    public long DELAY = 1000;
    private Timer timer = new Timer();

    public OrderQuantityTextWatcher(CommodityViewModel commodityViewModel, EditText editTextQuantity, Context context) {
        this.commodityViewModel = commodityViewModel;
        this.editTextQuantity = editTextQuantity;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(final Editable editable) {
        final int orderQuantity = getOrderQuantity(editable);
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                commodityViewModel.setQuantityEntered(orderQuantity);
                if (orderQuantity > 0) {
                    EventBus.getDefault().post(new OrderQuantityChangedEvent(orderQuantity, commodityViewModel));
                }
            }
        }, DELAY);

        if (orderQuantity <= 0) {
            editTextQuantity.setError(context.getResources().getString(R.string.orderQuantityMustBeGreaterThanZero));
        }
    }

    private int getOrderQuantity(Editable e) {
        final String quantityString = e.toString();
        try {
            return Integer.parseInt(quantityString);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
