/*
 * Copyright (c) 2014, Thoughtworks Inc
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

package org.clintonhealthaccess.lmis.app.adapters.strategies;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;

import java.io.Serializable;

abstract public class CommodityDisplayStrategy{
    public void apply(BaseCommodityViewModel commodityViewModel, CheckBox checkboxCommoditySelected, TextView alternativeText) {
        if (!allowClick(commodityViewModel)) {
            checkboxCommoditySelected.setVisibility(View.INVISIBLE);
            alternativeText.setVisibility(View.VISIBLE);
            alternativeText.setText(getMessage());
        }
    }

    abstract public boolean allowClick(BaseCommodityViewModel commodityViewModel);

    public String getMessage() {
        return "Not In Stock";
    }

    public boolean hideCommodities() {
        return false;
    }

    public String getEmptyMessage() {
        return "No Commodities Available";
    }

    public static final CommodityDisplayStrategy DISALLOW_CLICK_WHEN_OUT_OF_STOCK = new CommodityDisplayStrategy() {
        @Override
        public boolean allowClick(BaseCommodityViewModel commodityViewModel) {
            return !commodityViewModel.stockIsFinished();
        }
    };

    public static final CommodityDisplayStrategy ALLOW_CLICK_WHEN_OUT_OF_STOCK = new CommodityDisplayStrategy() {
        @Override
        public boolean allowClick(BaseCommodityViewModel commodityViewModel) {
            return true;
        }
    };

    public static final CommodityDisplayStrategy ALLOW_ONLY_LGA_COMMODITIES = new CommodityDisplayStrategy() {
        @Override
        public boolean allowClick(BaseCommodityViewModel commodityViewModel) {
            return commodityViewModel.getCommodity().isLGA();
        }

        @Override
        public String getMessage() {
            return "Can't Be Ordered Here";
        }

        @Override
        public String getEmptyMessage() {
            return "Commodities for this category Can Not be Ordered Here";
        }

        @Override
        public boolean hideCommodities() {
            return true;
        }
    };
}
