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

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.services.StockService;

import roboguice.RoboGuice;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class QuantityTextWatcher implements TextWatcher {
    private final EditText editTextQuantity;
    private final BaseCommodityViewModel commodityViewModel;

    @Inject
    StockService stockService;

    public QuantityTextWatcher(EditText editTextQuantity, BaseCommodityViewModel commodityViewModel) {
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
            int quantity = getIntFromString(value);
            Log.i("Entered", String.format("%d", quantity));
            Log.i("Entered", String.format("%s", value));
            int stock_level = stockService.getStockLevelFor(commodityViewModel.getCommodity());
            commodityViewModel.setQuantityEntered(quantity);
            if (quantity > stock_level) {
                editTextQuantity.setError(String.format("The quantity entered is greater than Stock available (%d)", stock_level));
            }
        }
    }
}
