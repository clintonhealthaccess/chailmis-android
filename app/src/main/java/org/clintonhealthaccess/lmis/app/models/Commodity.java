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
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;

@DatabaseTable(tableName = "commodities")
public class Commodity implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String lmisId;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private int orderDuration;

    @DatabaseField(canBeNull = false, foreign = true)
    private Category category;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Aggregation aggregation;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<StockItem> stockItems;

    public Commodity() {
        // ormlite wants it
    }

    public Commodity(String name) {
        this.lmisId = name;
        this.name = name;
    }

    public Commodity(String id, String name) {
        this.lmisId = id;
        this.name = name;
    }

    public Commodity(String name, Category category) {
        this(name);
        this.category = category;
    }

    public Commodity(String id, String name, Aggregation aggregation) {
        this.lmisId = id;
        this.name = name;
        this.aggregation = aggregation;
    }

    public Commodity(String name, Category category, Aggregation aggregation) {
        this(name, category);
        this.aggregation = aggregation;
    }


    public String getName() {
        return name;
    }

    public int getOrderDuration() {
        return orderDuration;
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

        return lmisId.equals(commodity.lmisId);
    }

    @Override
    public int hashCode() {
        return lmisId.hashCode();
    }

    public String getLmisId() {
        return lmisId;
    }

    //BAD
    public void setCategory(Category category) {
        this.category = category;
    }

    public StockItem getStockItem() {
        try {
            return copyOf(stockItems).get(0);
        } catch (Exception e) {
            throw new LmisException(String.format("Stock for commodity %s not found", name));
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

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
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
}
