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

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "loss_items")
public class LossItem implements Serializable, Snapshotable {

    public static final String WASTED = "waste";
    public static final String MISSING = "missing";
    public static final String EXPIRED = "expire";
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Loss loss;

    @DatabaseField(canBeNull = false)
    private int wastages;

    @DatabaseField(canBeNull = false)
    private int missing;

    @DatabaseField(canBeNull = false)
    private int expiries;

    public LossItem(Commodity commodity) {
        this.commodity = commodity;
    }

    public LossItem(Commodity commodity, int expiries) {
        this(commodity);
        this.expiries = expiries;
    }

    public int getNewStockOnHand() {
        return commodity.getStockOnHand() - getTotalLosses();
    }

    public int getTotalLosses() {
        return missing + wastages + expiries;
    }


    @Override
    public List<CommodityActivityValue> getActivitiesValues() {
        List<CommodityActivity> activities = ImmutableList.copyOf(getCommodity().getCommodityActivitiesSaved());
        List<CommodityActivityValue> values = new ArrayList<>();
        for (CommodityActivity activity : activities) {
            String activityType = activity.getActivityType().toLowerCase();
            if (activityType.contains(WASTED)) {
                values.add(new CommodityActivityValue(activity, wastages));
            }

            if (activityType.contains(MISSING)) {
                values.add(new CommodityActivityValue(activity, missing));
            }

            if (activityType.contains(EXPIRED)) {
                values.add(new CommodityActivityValue(activity, expiries));
            }

        }
        return values;
    }
}
