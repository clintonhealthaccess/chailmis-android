package org.clintonhealthaccess.lmis.app.watchers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.services.StockService;

import roboguice.RoboGuice;

public class QuantityTextWatcher implements TextWatcher {
    private final EditText editTextQuantity;
    private final CommodityViewModel commodityViewModel;

    @Inject
    StockService stockService;

    public QuantityTextWatcher(EditText editTextQuantity, CommodityViewModel commodityViewModel) {
        this.editTextQuantity = editTextQuantity;
        this.commodityViewModel = commodityViewModel;
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
            int stock_level = stockService.getStockLevelFor(commodityViewModel.getCommodity());
            if (quantity > stock_level) {
                editTextQuantity.setError(String.format("The quantity entered is greater than Stock available (%d)", stock_level));
            }
        }
    }
}
