/*
 * Copyright (c) 2014, Clinton Health Access Initiative
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.adapters;

import android.text.Editable;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.utils.ViewHelpers;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

class OrderQuantityTextWatcher extends LmisTextWatcher {
    private final OrderCommodityViewModel orderCommodityViewModel;
    private final Spinner spinnerUnexpectedQuantityReasons;
    private final EditText editTextOrderQuantity;
    private SelectedOrderCommoditiesAdapter selectedOrderCommoditiesAdapter;

    public OrderQuantityTextWatcher(SelectedOrderCommoditiesAdapter selectedOrderCommoditiesAdapter, OrderCommodityViewModel orderCommodityViewModel, Spinner spinnerUnexpectedQuantityReasons, EditText editTextOrderQuantity) {
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
