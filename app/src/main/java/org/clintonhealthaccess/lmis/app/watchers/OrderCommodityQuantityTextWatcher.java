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
