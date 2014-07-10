package org.clintonhealthaccess.lmis.app.watchers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;

import roboguice.RoboGuice;

public class QuantityTextWatcher implements TextWatcher {
    private final EditText editTextQuantity;
    private final Commodity commodity;

    @Inject
    StockService stockService;

    public QuantityTextWatcher(EditText editTextQuantity, Commodity commodity) {
        this.editTextQuantity = editTextQuantity;
        this.commodity = commodity;
        RoboGuice.getInjector(editTextQuantity.getContext()).injectMembers(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String value = editable.toString();
        if (!value.isEmpty()) {
            int quantity = Integer.parseInt(value);
            int stock_level = stockService.getStockLevelFor(commodity);
            if (quantity > stock_level) {
                editTextQuantity.setError("The quantity entered is greater than Stock available.");
            }
        }

    }
}
