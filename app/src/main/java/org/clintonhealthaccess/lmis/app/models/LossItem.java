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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.ToString;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.String.format;
import static org.clintonhealthaccess.lmis.app.models.LossReason.getLossCommodityActions;

@ToString
@DatabaseTable(tableName = "loss_items")
public class LossItem extends BaseItem implements Serializable, Snapshotable {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Loss loss;

    @ForeignCollectionField(eager = false, maxEagerLevel = 2)
    private ForeignCollection<LossItemDetail> lossItemDetailCollection;

    private List<LossItemDetail> lossItemDetails;

    @Deprecated
    public LossItem() {

    }

    public LossItem(Commodity commodity) {
        this(commodity, 0);
    }

    public LossItem(Commodity commodity, int expiries) {
        lossItemDetails = from(getLossCommodityActions(commodity)).transform(new Function<CommodityAction, LossItemDetail>() {
            @Override
            public LossItemDetail apply(CommodityAction input) {
                return new LossItemDetail(LossItem.this, LossReason.of(input));
            }
        }).toList();
        this.commodity = commodity;
        setLossAmount(LossReason.EXPIRED, expiries);
    }

    public int getNewStockOnHand() {
        return commodity.getStockOnHand() - getTotalLosses();
    }

    public int getTotalLosses() {
        int result = 0;
        if(lossItemDetails == null){
            lossItemDetails = copyOf(lossItemDetailCollection);
        }
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
        List<CommodityAction> activities = copyOf(getLossCommodityActions(getCommodity()));
        List<CommoditySnapshotValue> values = new ArrayList<>();
        for (CommodityAction activity : activities) {
            selectActivity(values, activity);
        }
        return values;
    }

    @Override
    public Date getDate() {
        return loss.getCreated();
    }

    private void selectActivity(List<CommoditySnapshotValue> values, CommodityAction activity) {
        LossItemDetail lossItemDetailForActivityType = getLossItemDetail(LossReason.of(activity));
        values.add(new CommoditySnapshotValue(activity, lossItemDetailForActivityType.getValue()));
    }

    private LossItemDetail getLossItemDetail(final LossReason lossReason) {
        List<LossItemDetail> lossItemDetailsForActivityType = from(lossItemDetails).filter(new Predicate<LossItemDetail>() {
            @Override
            public boolean apply(LossItemDetail input) {
                return lossReason.name().contains(input.getReason());
            }
        }).toList();
        LossItemDetail lossItemDetailForActivityType;
        try {
            lossItemDetailForActivityType = lossItemDetailsForActivityType.get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new LmisException(format("Cannot find loss item detail for %s", lossReason), e);
        }
        return lossItemDetailForActivityType;
    }

    public void setLossAmount(LossReason reason, int amount) {
        getLossItemDetail(reason).setValue(amount);
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

    @Override
    public Integer getQuantity() {
        return getTotalLosses();
    }

    @Override
    public Date created() {
        return loss.getCreated();
    }
}
