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
import com.google.common.collect.Lists;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@DatabaseTable
public class CommodityAction implements Serializable {

    @DatabaseField(canBeNull = true, foreign = true)
    private Commodity commodity;
    @DatabaseField(id = true, uniqueIndex = true)
    private String id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String activityType;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<CommodityActionValue> commodityActionValueCollection;

    @ForeignCollectionField(eager = false, maxEagerLevel = 3)
    private ForeignCollection<CommodityActionDataSet> commodityActionDataSets;

    private List<CommodityActionDataSet> transientCommodityActionDataSets;

    private List<CommodityActionValue> commodityActionValues = new ArrayList<>();

    public CommodityAction() {
        //Orm Lite likes
    }

    public CommodityAction(Commodity commodity, String id, String name, String activityType) {
        this(id, name, activityType);
        this.commodity = commodity;
    }

    public CommodityAction(String id, String name, String activityType) {
        this.id = id;
        this.name = name;
        this.activityType = activityType;
    }

    public CommodityAction(String dataElement) {
        this.id = dataElement;
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

    public List<CommodityActionDataSet> getCommodityActionDataSets() {
        if (commodityActionDataSets == null) {
            return newArrayList();
        }
        return ImmutableList.copyOf(commodityActionDataSets);
    }

    public void addTransientCommodityActionDataSets(List<CommodityActionDataSet> commodityActionDataSets) {
        this.transientCommodityActionDataSets = commodityActionDataSets;
    }

    public List<CommodityActionDataSet> getTransientCommodityActionDataSets() {
        return transientCommodityActionDataSets;
    }

    public String getActivityType() {
        return activityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommodityAction)) return false;

        CommodityAction that = (CommodityAction) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "CommodityAction{" +
                "id='" + id + '\'' + "activityType='" + activityType + '\'' + "name='" + name + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public void setCommodity(Commodity commodity) {
        this.commodity = commodity;
    }

}
