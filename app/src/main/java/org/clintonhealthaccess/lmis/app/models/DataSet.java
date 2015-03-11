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
import com.thoughtworks.dhis.models.DataElement;

import org.clintonhealthaccess.lmis.app.utils.Helpers;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "datasets")
public class DataSet implements Serializable {

    public static final String ALLOCATED = "LMIS Commodities Allocated";
    public static final String CALCULATED = "LMIS Commodities Calculated";
    public static final String DEFAULT = "LMIS Commodities Default";

    public static final String NAME = "name";


    @DatabaseField
    private String description;

    @DatabaseField(id = true, uniqueIndex = true)
    private String id;
    @DatabaseField(columnName = NAME, uniqueIndex = true)
    private String name;
    @DatabaseField
    private String periodType;

    private List<DataElement> dataElements;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<CommodityActionDataSet> commodityActionDataSets;

    public DataSet(String id) {
        this.id = id;
    }

    public DataSet() {
        //ormLite likes
    }

    public DataSet(String id, String name, String periodType){
        this.id = id;
        this.name = name;
        this.periodType = periodType;
    }
    public DataSet(com.thoughtworks.dhis.models.DataSet rawDataSet) {
        id = rawDataSet.getId();
        name = rawDataSet.getName();
        description = rawDataSet.getDisplayName();
        periodType = rawDataSet.getPeriodType();
        dataElements = rawDataSet.getDataElements();
    }

    public com.thoughtworks.dhis.models.DataSet toRawDataSet() {
        return com.thoughtworks.dhis.models.DataSet.builder()
                .id(id).name(name).displayName(description).periodType(periodType)
                .dataElements(dataElements).build();
    }

    public String getPeriod(Date date) {
        System.out.println("Period Type is " + getPeriodType());
        OrderCycle cycle = Helpers.getOrderCycle(getPeriodType());
        return cycle.getPeriod(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSet)) return false;

        DataSet dataSet = (DataSet) o;

        if (id != null ? !id.equals(dataSet.id) : dataSet.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public List<DataElement> getDataElements() {
        return dataElements;
    }

    public String getName() {
        return name;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public String getPeriodType() {
        return periodType;
    }
}
