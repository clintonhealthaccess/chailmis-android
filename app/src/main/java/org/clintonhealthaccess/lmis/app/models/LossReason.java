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

package org.clintonhealthaccess.lmis.app.models;

import com.google.common.base.Predicate;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;

public enum LossReason {
    EXPIRED("Expired"),
    WASTED("Wastage"),
    MISSING("Missing"),
    VVM_CHANGE("VVM Change"),
    BREAKAGE("Breakage"),
    FROZEN("Frozen"),
    LABEL_REMOVED("Label Removed"),
    OTHERS("Others");

    private String label;

    LossReason(String label) {
        this.label = label;
    }

    public static LossReason of(CommodityAction commodityAction) {
        return valueOf(commodityAction.getActivityType());
    }

    public static List<CommodityAction> getLossCommodityActions(Commodity commodity) {
        return from(commodity.getCommodityActionsSaved()).filter(new Predicate<CommodityAction>() {
            @Override
            public boolean apply(CommodityAction input) {
                String activityType = input.getActivityType();
                return LossReason.isValidLossReason(activityType);
            }
        }).toList();
    }

    private static boolean isValidLossReason(String activityType) {
        try {
            valueOf(activityType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getLabel() {
        return label;
    }
}
