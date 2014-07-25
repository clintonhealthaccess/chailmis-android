package org.clintonhealthaccess.lmis.app.adapters;

import android.text.Editable;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.utils.ViewHelpers;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

class OrderQuantityTextWatcher extends LmisTextWatcher {
    private SelectedOrderCommoditiesAdapter selectedOrderCommoditiesAdapter;
    private final CommodityViewModel orderCommodityViewModel;
    private final Spinner spinnerUnexpectedQuantityReasons;
    private final EditText editTextOrderQuantity;

    public OrderQuantityTextWatcher(SelectedOrderCommoditiesAdapter selectedOrderCommoditiesAdapter, CommodityViewModel orderCommodityViewModel, Spinner spinnerUnexpectedQuantityReasons, EditText editTextOrderQuantity) {
        this.selectedOrderCommoditiesAdapter = selectedOrderCommoditiesAdapter;
        this.orderCommodityViewModel = orderCommodityViewModel;
        this.spinnerUnexpectedQuantityReasons = spinnerUnexpectedQuantityReasons;
        this.editTextOrderQuantity = editTextOrderQuantity;
    }

    @Override
    public void afterTextChanged(final Editable editable) {
        int quantityInt = ViewHelpers.getIntFromString(editable.toString());
        orderCommodityViewModel.setQuantityEntered(quantityInt);
        if (orderCommodityViewModel.quantityIsUnexpected()) {
            String commodityName = orderCommodityViewModel.getName();
            String message = getErrorMessage(commodityName);
            Toast.makeText(selectedOrderCommoditiesAdapter.getContext(), message, Toast.LENGTH_LONG).show();
        }
        selectedOrderCommoditiesAdapter.setupUnexpectedReasonsSpinnerVisibility(orderCommodityViewModel, spinnerUnexpectedQuantityReasons);
        if (quantityInt <= 0) {
            editTextOrderQuantity.setError(selectedOrderCommoditiesAdapter.getContext().getString(R.string.orderQuantityMustBeGreaterThanZero));
        }
    }

    private String getErrorMessage(String commodityName) {
        String formatString = selectedOrderCommoditiesAdapter.getContext().getString(R.string.unexpected_order_quantity_error);
        return String.format(formatString, orderCommodityViewModel.getExpectedOrderQuantity(), commodityName);
    }
}
