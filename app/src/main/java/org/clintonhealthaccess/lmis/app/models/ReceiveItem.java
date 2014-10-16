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
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "receive_items")
public class ReceiveItem extends BaseItem implements Snapshotable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Receive receive;

    @DatabaseField(canBeNull = false)
    private int quantityAllocated;

    @DatabaseField(canBeNull = false)
    private int quantityReceived;

    public ReceiveItem(Commodity commodity, int quantityAllocated, int quantityReceived) {
        this.commodity = commodity;
        this.quantityAllocated = quantityAllocated;
        this.quantityReceived = quantityReceived;
    }

    public int getDifference() {
        return quantityAllocated - quantityReceived;
    }

    @Override
    public List<CommoditySnapshotValue> getActivitiesValues() {
        Function<CommodityAction, CommoditySnapshotValue> forReceivedAction = new Function<CommodityAction, CommoditySnapshotValue>() {
            @Override
            public CommoditySnapshotValue apply(CommodityAction input) {
                if (receive.isRecievingFromLGA()) {
                    return new CommoditySnapshotValue(input, quantityReceived, getAllocationPeriod());
                } else {
                    return new CommoditySnapshotValue(input, quantityReceived);
                }
            }
        };
        Function<CommodityAction, CommoditySnapshotValue> forReceiveDateAction = new Function<CommodityAction, CommoditySnapshotValue>() {
            @Override
            public CommoditySnapshotValue apply(CommodityAction input) {
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                if (receive.isRecievingFromLGA()) {
                    return new CommoditySnapshotValue(input, today, getAllocationPeriod());
                } else {
                    return new CommoditySnapshotValue(input, today);
                }
            }
        };

        Function<CommodityAction, CommoditySnapshotValue> forReceivedSource = new Function<CommodityAction, CommoditySnapshotValue>() {
            @Override
            public CommoditySnapshotValue apply(CommodityAction input) {
                String source = receive.getSource();
                return new CommoditySnapshotValue(input, source);
            }
        };

        List<CommoditySnapshotValue> receivedValues = filterCommodityActions(DataElementType.RECEIVED.getActivity()).transform(forReceivedAction).toList();
        List<CommoditySnapshotValue> receiveDateValues = filterCommodityActions(DataElementType.RECEIVE_DATE.getActivity()).transform(forReceiveDateAction).toList();
        List<CommoditySnapshotValue> receiveSourceValues = filterCommodityActions(DataElementType.RECEIVE_SOURCE.getActivity()).transform(forReceivedSource).toList();
        List<CommoditySnapshotValue> result = newArrayList(receivedValues);
        result.addAll(receiveDateValues);
        result.addAll(receiveSourceValues);
        return result;
    }

    private String getAllocationPeriod() {
        Allocation allocation = receive.getAllocation();
        if (allocation != null) {
            return allocation.getPeriod();
        }
        return "";

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
        return quantityReceived;
    }
}
