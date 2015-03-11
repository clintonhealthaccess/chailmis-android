/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "commodityActionDataSet")
public class CommodityActionDataSet implements Serializable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private CommodityAction commodityAction;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private DataSet dataSet;

    public CommodityActionDataSet() {
        //orm-lite likes
    }

    public CommodityActionDataSet(CommodityAction commodityAction, DataSet dataSet) {
        this.commodityAction = commodityAction;
        this.dataSet = dataSet;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public static List<CommodityActionDataSet> generateCommodityActionDataSets(
            CommodityAction commodityAction, List<DataSet> dataSets) {
        List<CommodityActionDataSet> commodityActionDataSets = new ArrayList<>();
        for (DataSet dataSet : dataSets) {
            commodityActionDataSets.add(new CommodityActionDataSet(commodityAction, dataSet));
        }
        return commodityActionDataSets;
    }

    public CommodityAction getCommodityAction() {
        return commodityAction;
    }
}
