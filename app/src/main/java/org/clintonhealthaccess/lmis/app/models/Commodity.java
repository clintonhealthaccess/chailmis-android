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

import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;

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

    @DatabaseField(canBeNull = false)
    private boolean nonLGA = false;

    @ForeignCollectionField
    private ForeignCollection<StockItem> stockItems;

    @ForeignCollectionField
    private Collection<CommodityAction> commodityActionsSaved;

    @DatabaseField(canBeNull = false)
    private boolean isDevice;

    @DatabaseField(canBeNull = false)
    private boolean isVaccine;

    private List<CommodityAction> commodityActions = newArrayList();

    public Commodity() {
        // ormlite wants it
    }

    public Commodity(String name) {
        this(name, name);
    }

    public Commodity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Commodity(String name, Category category) {
        this(name);
        this.category = category;
    }

    public Commodity(String id, String name, Category category, boolean nonLGA, boolean isDevice, boolean isVaccine) {
        this(id, name);
        this.category = category;
        this.nonLGA = nonLGA;
        this.isDevice = isDevice;
        this.isVaccine = isVaccine;
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

    public boolean isLGA() {
        return !nonLGA;
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
        for (CommodityAction commodityAction : new ArrayList<>(commodityActionsSaved)) {
            if (commodityAction.getActivityType().equalsIgnoreCase(activityType))
                return commodityAction;
        }
        return null;
    }

    public int getMinimumThreshold() {
        return getLatestValueFromCommodityActionByName(DataElementType.MIN_STOCK_QUANTITY.toString());
    }

    public int getMaximumThreshold() {
        return getLatestValueFromCommodityActionByName(DataElementType.MAX_STOCK_QUANTITY.toString());
    }

    public boolean isBelowThreshold() {
        return getStockOnHand() < getMinimumThreshold();
    }

    @Override
    public String toString() {
        return "Commodity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", commodityActionsSaved=" + commodityActionsSaved +
                ", commodityActions=" + commodityActions +
                '}';
    }

    public int calculateEmergencyPrepopulatedQuantity() {
        int max = getMaximumThreshold();
        return max - getStockOnHand();
    }

    public int getLatestValueFromCommodityActionByName(String actionName) {
        CommodityAction commodityAction = getCommodityAction(actionName);
        int defaultValue = 0;
        if (commodityAction != null) {
            CommodityActionValue actionLatestValue = commodityAction.getActionLatestValue();

            if (actionLatestValue != null && actionLatestValue.getValue() != null) {
                String value = (actionLatestValue.getValue().contains(".")) ? actionLatestValue.getValue()
                        .substring(0, actionLatestValue.getValue().indexOf(".")) : actionLatestValue.getValue();

                return Integer.parseInt(value);
            }
        }
        return defaultValue;
    }

    public int calculateRoutinePrePopulatedQuantity() {
        return getLatestValueFromCommodityActionByName(DataElementType.PROJECTED_ORDER_AMOUNT.toString());
    }

    public Integer getAMC() {
        return getLatestValueFromCommodityActionByName(DataElementType.AMC.toString());
    }

    public void setIsDevice(boolean isDevice) {
        this.isDevice = isDevice;
    }

    public void setIsVaccine(boolean isVaccine) {
        this.isVaccine = isVaccine;
    }

    public boolean isVial() {
        return name.toLowerCase().contains("vial");
    }

    public int dosesPerVial() {
        return isVial() ? findDosesPerVial() : 1;
    }

    private int findDosesPerVial() {
        String trimmedName = name.trim();
        String dosesString = trimmedName.substring(trimmedName.lastIndexOf(" ")).trim();
        String numberOfDosesString = dosesString.substring(0, dosesString.indexOf("_"));
        try {
            return Integer.parseInt(numberOfDosesString);
        } catch (NumberFormatException e) {
            Log.e("Commodity", e.getMessage());
            return 1;
        }
    }

    public List<CommodityAction> getCommodityActionsSaved() {
        if (commodityActionsSaved == null) {
            return newArrayList();
        }
        return ImmutableList.copyOf(commodityActionsSaved);
    }

    public Category getCategory() {
        return category;
    }

    public boolean isDevice() {
        return isDevice;
    }

    public boolean isVaccine() {
        return isVaccine;
    }

    public boolean isNonLGA() {
        return nonLGA;
    }

    public String getId() {
        return id;
    }

    public List<CommodityAction> getCommodityActions() {
        return commodityActions;
    }

    public void setNonLGA(boolean nonLGA) {
        this.nonLGA = nonLGA;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

}
