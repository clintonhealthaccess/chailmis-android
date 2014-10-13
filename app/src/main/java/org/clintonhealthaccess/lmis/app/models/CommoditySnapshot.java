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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable
public class CommoditySnapshot {

    public static final String PERIOD = "period";
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = "commodityActivity_id", foreign = true, foreignAutoRefresh = true)
    private CommodityAction commodityAction;

    @DatabaseField(canBeNull = false)
    private String value;

    @DatabaseField(defaultValue = "false")
    private boolean synced;

    @DatabaseField(defaultValue = "false")
    private boolean smsSent;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = PERIOD)
    private String period;

    @DatabaseField(canBeNull = true)
    private String attributeOptionCombo;

    public static DataValueSet toDataValueSet(List<CommoditySnapshot> snapshotsToSync, String orgUnit) {
        DataValueSet dataValueSet = new DataValueSet();
        for (CommoditySnapshot snapshot : snapshotsToSync) {
            dataValueSet.getDataValues().add(snapshot.toDataValue(orgUnit));
        }
        return dataValueSet;
    }

    public CommoditySnapshot(Commodity commodity, CommodityAction commodityAction, String value) {
        this.commodity = commodity;
        this.commodityAction = commodityAction;
        this.value = value;
        this.synced = false;
        this.period = commodityAction.getPeriod();
    }

    public CommoditySnapshot(CommoditySnapshotValue commoditySnapshotValue) {
        this(commoditySnapshotValue.getCommodityAction().getCommodity(), commoditySnapshotValue.getCommodityAction(), commoditySnapshotValue.getValue());
        if (commoditySnapshotValue.getPeriod() != null) {
            this.period = commoditySnapshotValue.getPeriod();
        }
    }

    public CommoditySnapshot(Commodity commodity, CommodityAction commodityAction, String value, String attributeOptionComboId) {
        this(commodity, commodityAction, value);
        this.attributeOptionCombo = attributeOptionComboId;
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

    private DataValue toDataValue(String orgUnit) {
        return DataValue.builder().value(String.valueOf(getValue())).
                dataSet(getCommodityAction().getDataSet().getId()).
                dataElement(getCommodityAction().getId()).
                period(getPeriod()).orgUnit(orgUnit).
                attributeOptionCombo(getAttributeOptionCombo()).build();
    }
}
