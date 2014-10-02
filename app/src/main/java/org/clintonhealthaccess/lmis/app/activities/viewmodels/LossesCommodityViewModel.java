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

package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.LossReason;

import java.util.HashMap;
import java.util.Map;

import static org.clintonhealthaccess.lmis.app.models.LossReason.EXPIRED;
import static org.clintonhealthaccess.lmis.app.models.LossReason.MISSING;
import static org.clintonhealthaccess.lmis.app.models.LossReason.WASTED;
import static org.clintonhealthaccess.lmis.app.models.LossReason.getLossCommodityActions;

public class LossesCommodityViewModel extends BaseCommodityViewModel {
    private Map<LossReason, Integer> losses = new HashMap<>();

    public LossesCommodityViewModel(Commodity commodity) {
        super(commodity);
        for (CommodityAction commodityAction : getLossCommodityActions(commodity)) {
            losses.put(LossReason.valueOf(commodityAction.getActivityType()), 0);
        }
    }

    public int getWastage() {
        return losses.get(WASTED);
    }

    public int getMissing() {
        return losses.get(MISSING);
    }

    public int getExpiries() {
        return losses.get(EXPIRED);
    }

    public void setMissing(int missing) {
        losses.put(MISSING, missing);
    }

    public void setWastages(int wastage) {
        losses.put(WASTED, wastage);
    }

    public void setExpiries(int expired) {
        losses.put(EXPIRED, expired);
    }

    public int totalLosses() {
        return getWastage() + getExpiries() + getMissing();
    }

    public boolean isValid() {
        return !(getMissing() == 0 && getExpiries() == 0 && getWastage() == 0) && totalLosses() <= getStockOnHand();
    }

    public LossItem getLossItem() {
        LossItem lossItem = new LossItem(getCommodity());
        for (CommodityAction commodityAction : getLossCommodityActions(getCommodity())) {
            LossReason lossReason = LossReason.valueOf(commodityAction.getActivityType());
            lossItem.setLossAmount(lossReason, losses.get(lossReason));
        }
        return lossItem;
    }
}
