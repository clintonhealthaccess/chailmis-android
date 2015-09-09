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
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DatabaseTable
public class CommoditySnapshot {

    public static final String PERIOD_DATE = "period_date";

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, columnName = "commodityActivity_id", foreign = true, foreignAutoRefresh = true)
    private CommodityAction commodityAction;

    @DatabaseField(canBeNull = false)
    private String value;

    @DatabaseField(defaultValue = "false")
    private boolean synced;

    @DatabaseField(defaultValue = "false")
    private boolean smsSent;

    @DatabaseField(canBeNull = false, foreign = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd", columnName = PERIOD_DATE)
    private Date periodDate;

    @DatabaseField(canBeNull = true)
    private String attributeOptionCombo;

    @Override
    public String toString() {
        return id + " " + value + " " + commodityAction + " Commodity: " + commodity.getName() + " " + periodDate;
    }

    public CommoditySnapshot(Commodity commodity, CommodityAction commodityAction,
                             String value, Date periodDate) {
        this.commodity = commodity;
        this.commodityAction = commodityAction;
        this.value = value;
        this.synced = false;
        this.periodDate = periodDate;
    }

    public CommoditySnapshot(CommoditySnapshotValue commoditySnapshotValue) {
        this(commoditySnapshotValue.getCommodityAction().getCommodity(),
                commoditySnapshotValue.getCommodityAction(),
                commoditySnapshotValue.getValue(), commoditySnapshotValue.getPeriodDate());

        // this.attributeOptionCombo = attributeOptionComboId;
    }
    public CommoditySnapshot() {
        //ormLite likes
    }

    public void incrementValue(String value) {
        try {
            int numberValue = Integer.parseInt(value);
            int setValue = Integer.parseInt(this.value);
            this.value = String.valueOf(numberValue + setValue);

        } catch (NumberFormatException ex) {
            this.value = value;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static DataValueSet toDataValueSet(List<CommoditySnapshot> snapshotsToSync, String orgUnit) {
        DataValueSet dataValueSet = new DataValueSet();
        for (CommoditySnapshot snapshot : snapshotsToSync) {
            dataValueSet.getDataValues().addAll(snapshot.toDataValues(orgUnit));
        }
        return dataValueSet;
    }

    public List<DataValue> toDataValues(String orgUnit) {
        List<DataValue> dataValues = new ArrayList<>();
        for (CommodityActionDataSet commodityActionDataSet : commodityAction.getCommodityActionDataSets()) {
            dataValues.add(toDataValue(orgUnit, commodityActionDataSet.getDataSet()));
        }
        return dataValues;
    }

    private DataValue toDataValue(String orgUnit, DataSet dataSet) {
        return DataValue.builder().value(String.valueOf(value)).
                dataSet(dataSet.getId()).
                dataElement(commodityAction.getId()).
                period(dataSet.getPeriod(periodDate)).orgUnit(orgUnit).
                attributeOptionCombo(attributeOptionCombo).build();
    }

    public boolean isSmsSent() {
        return smsSent;
    }

    public void setSmsSent(boolean smsSent) {
        this.smsSent = smsSent;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public boolean isSynced() {
        return synced;
    }

    public Date getPeriodDate(){
        return periodDate;
    }

    public String getValue(){
        return value;
    }

    public CommodityAction getCommodityAction() {
        return commodityAction;
    }
}
