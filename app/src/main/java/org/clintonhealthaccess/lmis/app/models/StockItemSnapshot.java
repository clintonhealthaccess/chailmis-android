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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.ToString;

import static com.thoughtworks.dhis.models.DataElementType.STOCK_ON_HAND;

@DatabaseTable
@ToString
public class StockItemSnapshot implements Snapshotable {

    @DatabaseField(generatedId = true)
    protected int id;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    private Date created;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false)
    private int minimumStockLevel;

    @DatabaseField(canBeNull = false)
    private int maximumStockLevel;

    @DatabaseField(canBeNull = false)
    private boolean stockOut;

    public StockItemSnapshot() {
        //OrlLite likes
    }

    public StockItemSnapshot(Commodity commodity, Date created, int quantity) {
        this.commodity = commodity;
        this.created = created;
        this.quantity = quantity;
        this.minimumStockLevel = quantity;
        this.maximumStockLevel = quantity;
        setInitialStockOut(quantity);
    }

    private void setStockOut(int quantity) {
        if (quantity <= 0) {
            stockOut = true;
        }
    }

    private void setInitialStockOut(int quantity) {
        if (quantity <= 0) {
            stockOut = true;
        }else{
            stockOut=false;
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (quantity < this.minimumStockLevel) {
            this.minimumStockLevel = quantity;
        }
        if (quantity > this.maximumStockLevel) {
            this.maximumStockLevel = quantity;
        }
        setStockOut(quantity);
    }

    public Date getCreated() {
        return created;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    @Override
    public List<CommoditySnapshotValue> getActivitiesValues() throws Exception {
        List<CommoditySnapshotValue> commoditySnapshotValues = new ArrayList<>(1);
        commoditySnapshotValues.add(new CommoditySnapshotValue(
                getCommodity().getCommodityAction(STOCK_ON_HAND.getActivity()), getQuantity(), getDate()));
        return commoditySnapshotValues;
    }

    @Override
    public Date getDate() {
        return getCreated();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockItemSnapshot that = (StockItemSnapshot) o;

        if (quantity != that.quantity) return false;
        if (!commodity.equals(that.commodity)) return false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (!dateFormat.format(created).equals(dateFormat.format(that.created))) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = commodity.hashCode();
        result = 31 * result + created.hashCode();
        result = 31 * result + quantity;
        return result;
    }

    public int minimumStockLevel() {
        return minimumStockLevel;
    }

    public int maximumStockLevel() {
        return this.maximumStockLevel;
    }

    public boolean isStockOut() {
        return stockOut;
    }

    public int getId() {
        return id;
    }
}
