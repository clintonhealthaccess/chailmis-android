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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.utils.Helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable
public class CommodityAction implements Serializable {
    @DatabaseField(canBeNull = false, foreign = true)
    private Commodity commodity;
    @DatabaseField(id = true, uniqueIndex = true)
    private String id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String activityType;
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private DataSet dataSet;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<CommodityActionValue> commodityActionValueCollection;

    private List<CommodityActionValue> commodityActionValues = new ArrayList<>();

    public CommodityAction() {
        //Orm Lite likes
    }


    public CommodityAction(Commodity actualCommodity, String id, String name, String activityType) {
        this.commodity = actualCommodity;
        this.id = id;
        this.name = name;
        this.activityType = activityType;
    }

    public CommodityAction(String dataElement) {
        this.id = dataElement;
    }

    public String getPeriod() {
        String periodType = getDataSet().getPeriodType();
        OrderCycle cycle = Helpers.getOrderCycle(periodType);
        return cycle.getPeriod(new Date());
    }


    public List<CommodityActionValue> getNotSavedCommodityActionValues() {
        return commodityActionValues;
    }


    public List<CommodityActionValue> getCommodityActionValueList() {
        return new ArrayList<>(this.commodityActionValueCollection);
    }

    public CommodityActionValue getActionLatestValue() {
        List<CommodityActionValue> commodityActionValueList = getCommodityActionValueList();
        if (commodityActionValueList != null && !commodityActionValueList.isEmpty()) {
            Collections.sort(commodityActionValueList, new Comparator<CommodityActionValue>() {
                @Override
                public int compare(CommodityActionValue lhs, CommodityActionValue rhs) {
                    return lhs.getPeriod().compareTo(rhs.getPeriod());
                }
            });

            return commodityActionValueList.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "CommodityAction{" +
                "activityType='" + activityType + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
