package org.clintonhealthaccess.lmis.app.watchers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;

public class OrderCommodityQuantityTextWatcher implements TextWatcher {

    private OrderCommodityViewModel commodityViewModel;
    private Context context;
    private EditText editTextOrderQuantity;
    private Handler handler;

    public OrderCommodityQuantityTextWatcher(OrderCommodityViewModel commodityViewModel, Context context, EditText editTextOrderQuantity, Handler handler) {
        this.commodityViewModel = commodityViewModel;
        this.context = context;
        this.editTextOrderQuantity = editTextOrderQuantity;
        this.handler = handler;
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
        commodityViewModel.setQuantityEntered(quantityInt);

        if (commodityViewModel.quantityIsUnexpected()) {
            String commodityName = commodityViewModel.getName();
            String message = String.format(context.getString(R.string.unexpected_order_quantity_error), commodityViewModel.getExpectedOrderQuantity(), commodityName);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        if (getOrderQuantity(editable) <= 0) {
            editTextOrderQuantity.setError(context.getString(R.string.orderQuantityMustBeGreaterThanZero));
        }

        handler.handle();
    }

    private int getOrderQuantity(Editable e) {
        final String quantityString = e.toString();
        try {
            return Integer.parseInt(quantityString);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public interface Handler {
        void handle();
    }
}
