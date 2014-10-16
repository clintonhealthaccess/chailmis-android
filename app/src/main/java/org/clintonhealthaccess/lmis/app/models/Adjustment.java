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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable
public class Adjustment implements Serializable, Snapshotable {
    public Adjustment() {
        //orm likes
    }

    @DatabaseField(foreign = true, canBeNull = false)
    Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private boolean positive;

    @DatabaseField(canBeNull = false)
    private String reason;

    public Adjustment(Commodity commodity, int quantityEntered, boolean positive, String reason) {
        this.commodity = commodity;
        this.quantity = quantityEntered;
        this.positive = positive;
        this.reason = reason;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    @Override
    public List<CommoditySnapshotValue> getActivitiesValues() {
        List<CommoditySnapshotValue> values = new ArrayList<>();
        values.add(new CommoditySnapshotValue(commodity.getCommodityAction(DataElementType.ADJUSTMENTS.getActivity()), quantity));
        values.add(new CommoditySnapshotValue(commodity.getCommodityAction(DataElementType.ADJUSTMENT_REASON.getActivity()), reason));
        return values;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getId() {
        return id;
    }

    public boolean isPositive() {
        return positive;
    }

    public String getReason() {
        return reason;
    }

    public String getType() {
        if (positive) {
            return "+";
        }
        return "-";
    }

    public int getNewStockOnHand() {
        int stock = commodity.getStockOnHand();
        if (positive) {
            return stock + quantity;
        }
        return stock - quantity;
    }
}
