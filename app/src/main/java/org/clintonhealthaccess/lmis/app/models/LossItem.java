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
import com.google.common.collect.ImmutableList;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

@DatabaseTable(tableName = "loss_items")
public class LossItem implements Serializable, Snapshotable {

    public static final String WASTED = "waste";
    public static final String MISSING = "missing";
    public static final String EXPIRED = "expire";

    private List<LossItemDetail> lossItemDetails;

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Loss loss;

    @Deprecated
    public LossItem() {
        this(null, 0);
    }

    public LossItem(Commodity commodity) {
        this(commodity, 0);
    }

    public LossItem(Commodity commodity, int expiries) {
        lossItemDetails = newArrayList(
                new LossItemDetail(this, WASTED),
                new LossItemDetail(this, MISSING),
                new LossItemDetail(this, EXPIRED)
        );
        this.commodity = commodity;
        this.setExpiries(expiries);
    }

    public int getNewStockOnHand() {
        return commodity.getStockOnHand() - getTotalLosses();
    }

    public int getTotalLosses() {
        int result = 0;
        for (LossItemDetail lossItemDetail : lossItemDetails) {
            result += lossItemDetail.getValue();
        }
        return result;
    }


    @Override
    public Commodity getCommodity() {
        return commodity;
    }

    @Override
    public List<CommoditySnapshotValue> getActivitiesValues() {
        List<CommodityAction> activities = ImmutableList.copyOf(getCommodity().getCommodityActionsSaved());
        List<CommoditySnapshotValue> values = new ArrayList<>();
        for (CommodityAction activity : activities) {
            selectActivity(values, activity);
        }
        return values;
    }

    private void selectActivity(List<CommoditySnapshotValue> values, CommodityAction activity) {
        final String activityType = activity.getActivityType().toLowerCase();
        List<LossItemDetail> lossItemDetailsForActivityType = from(lossItemDetails).filter(new Predicate<LossItemDetail>() {
            @Override
            public boolean apply(LossItemDetail input) {
                return activityType.contains(input.getReason());
            }
        }).toList();
        if(lossItemDetailsForActivityType.size() == 1) {
            values.add(new CommoditySnapshotValue(activity, lossItemDetailsForActivityType.get(0).getValue()));
        }
    }

    public void setWastages(int wastages) {
        lossItemDetails.get(0).setValue(wastages);
    }

    public void setMissing(int missing) {
        lossItemDetails.get(1).setValue(missing);
    }

    public void setExpiries(int expiries) {
        lossItemDetails.get(2).setValue(expiries);
    }

    public void setLoss(Loss loss) {
        this.loss = loss;
    }

    public void setCommodity(Commodity commodity) {
        this.commodity = commodity;
    }

    public List<LossItemDetail> getLossItemDetails() {
        return lossItemDetails;
    }
}
