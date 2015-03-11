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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.ToString;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.thoughtworks.dhis.models.DataElementType.DISPENSED;

@ToString
@DatabaseTable(tableName = "dispensingItems")
public class DispensingItem extends BaseItem implements Serializable, Snapshotable {

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Dispensing dispensing;

    public DispensingItem(Commodity commodity, int quantity) {
        this.commodity = commodity;
        this.quantity = quantity;
    }

    public DispensingItem() {
        //orm likes
    }

    public Commodity getCommodity() {
        return commodity;
    }

    @Override
    public List<CommoditySnapshotValue> getActivitiesValues() {
        Function<CommodityAction, CommoditySnapshotValue> forDispensed = new Function<CommodityAction, CommoditySnapshotValue>() {
            @Override
            public CommoditySnapshotValue apply(CommodityAction input) {
                return new CommoditySnapshotValue(input, quantity, dispensing.getCreated());
            }
        };

        List<CommoditySnapshotValue> dispensedValues = filterCommodityActions(DISPENSED.getActivity()).transform(forDispensed).toList();
        return dispensedValues;
    }

    @Override
    public Date getDate() {
        return dispensing.getCreated();
    }

    private FluentIterable<CommodityAction> filterCommodityActions(final String type) {
        List<CommodityAction> fields = copyOf(getCommodity().getCommodityActionsSaved());
        return from(fields).filter(new Predicate<CommodityAction>() {
            @Override
            public boolean apply(CommodityAction input) {
                return selectActivity(input, type);
            }
        });
    }

    private boolean selectActivity(CommodityAction input, String type) {
        return input.getActivityType().contains(type);
    }

    @Override
    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public Date created() {
        return dispensing.getCreated();
    }

    public void setDispensing(Dispensing dispensing) {
        this.dispensing = dispensing;
    }

}
