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
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static com.google.common.collect.ImmutableList.copyOf;

@Getter
@Setter
@DatabaseTable(tableName = "commodities")
public class Commodity implements Serializable {
    @DatabaseField(id = true, uniqueIndex = true)
    private String id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField
    private String orderFrequency;

    @DatabaseField(canBeNull = false, foreign = true)
    private Category category;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<StockItem> stockItems;

    @ForeignCollectionField(eager = true, maxEagerLevel = 5)
    private Collection<CommodityAction> commodityActionsSaved;

    private List<CommodityAction> commodityActions = new ArrayList<>();

    public Commodity() {
        // ormlite wants it
    }

    public Commodity(String name) {
        this.id = name;
        this.name = name;
    }

    public Commodity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Commodity(String name, Category category) {
        this(name);
        this.category = category;
    }


    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Commodity)) {
            return false;
        }

        Commodity commodity = (Commodity) o;

        return id.equals(commodity.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    //BAD
    public void setCategory(Category category) {
        this.category = category;
    }

    public StockItem getStockItem() {
        try {
            return copyOf(stockItems).get(0);
        } catch (Exception e) {
            throw new LmisException(String.format("Stock for commodity %s not found", name), e);
        }
    }

    public boolean isOutOfStock() {
        if (stockItems != null) {
            List<StockItem> items = ImmutableList.copyOf(stockItems);
            if (!items.isEmpty()) {
                return items.get(0).isFinished();
            }
        }

        return true;
    }

    public int getStockOnHand() {
        return getStockItem().getQuantity();
    }

    public void reduceStockOnHandBy(int quantity) {
        getStockItem().reduceQuantityBy(quantity);
    }

    public void increaseStockOnHandBy(int quantity) {
        getStockItem().increaseQuantityBy(quantity);
    }

    public String getOrderFrequency() {
        return orderFrequency;
    }

    public void setOrderFrequency(String orderFrequency) {
        this.orderFrequency = orderFrequency;
    }

    public CommodityAction getCommodityAction(String activityType) {
        for (CommodityAction commodityAction : new ArrayList<>(getCommodityActionsSaved())) {
            if (commodityAction.getActivityType().equalsIgnoreCase(activityType))
                return commodityAction;
        }
        return null;
    }

    public int getMinimumThreshold() {
        return getLatestValueFromCommodityActionByName(CommodityAction.MINIMUN_THRESHOLD);
    }

    public int getMaximuThreshold() {
        return getLatestValueFromCommodityActionByName(CommodityAction.MAXIMUM_THRESHOLD);
    }

    public boolean isBelowThreshold() {
        return getStockOnHand() < getMinimumThreshold();
    }

    public int calculateEmergencyPrepopulatedQuantity() {
        int max = getLatestValueFromCommodityActionByName(CommodityAction.MAXIMUM_THRESHOLD);
        return max - getStockOnHand();
    }

    private int getLatestValueFromCommodityActionByName(String actionName) {
        CommodityAction commodityAction = getCommodityAction(actionName);
        int defaultValue = 0;
        if (commodityAction != null) {
            CommodityActionValue actionLatestValue = commodityAction.getActionLatestValue();
            return (actionLatestValue != null) ? Integer.parseInt(actionLatestValue.getValue()) : defaultValue;
        } else {
            return defaultValue;
        }
    }

    public int calculateRoutinePrePopulatedQuantity() {
        return getLatestValueFromCommodityActionByName(CommodityAction.PROJECTED_ORDER_AMOUNT);
    }

    public Integer getAMC() {
        return getLatestValueFromCommodityActionByName(CommodityAction.AMC);
    }
}
