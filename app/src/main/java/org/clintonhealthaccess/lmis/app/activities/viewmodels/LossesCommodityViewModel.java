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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.clintonhealthaccess.lmis.app.models.LossReason.getLossCommodityActions;

public class LossesCommodityViewModel extends BaseCommodityViewModel {
    private Map<LossReason, Integer> losses = newHashMap();

    public LossesCommodityViewModel(Commodity commodity) {
        super(commodity);
        for (CommodityAction commodityAction : getLossCommodityActions(commodity)) {
            LossReason lossReason = LossReason.of(commodityAction);
            losses.put(lossReason, 0);
        }
    }

    public List<LossReason> getLossReasons() {
        ArrayList<LossReason> lossReasons = newArrayList(losses.keySet());
        Collections.sort(lossReasons);
        return lossReasons;
    }

    public int getLoss(LossReason reason) {
        return losses.get(reason);
    }

    public void setLoss(LossReason reason, int amount) {
        losses.put(reason, amount);
    }

    public int totalLosses() {
        int result = 0;
        for (Integer lossAmount : losses.values()) {
            result += lossAmount;
        }
        return result;
    }

    public boolean isValid() {
        return totalLosses() > 0 && totalLosses() <= getStockOnHand();
    }

    public LossItem getLossItem() {
        LossItem lossItem = new LossItem(getCommodity());
        for (CommodityAction commodityAction : getLossCommodityActions(getCommodity())) {
            LossReason lossReason = LossReason.of(commodityAction);
            lossItem.setLossAmount(lossReason, losses.get(lossReason));
        }
        return lossItem;
    }
}
