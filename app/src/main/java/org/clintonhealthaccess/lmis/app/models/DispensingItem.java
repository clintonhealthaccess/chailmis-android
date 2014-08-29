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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.services.Snapshotable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Collections2.filter;

@DatabaseTable(tableName = "dispensingItems")
public class DispensingItem implements Serializable, Snapshotable {
    public static final String DISPENSE = "dispense";

    Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false)
    private String commodityId;

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, foreign = true)
    private Dispensing dispensing;

    @DatabaseField
    private Date created;

    public DispensingItem(Commodity commodity, int quantity) {
        this.commodity = commodity;
        this.commodityId = commodity.getId();
        this.quantity = quantity;
        created = new Date();
    }

    public DispensingItem() {
        created = new Date();
    }

    public Commodity getCommodity() {
        return commodity;
    }

    @Override
    public CommodityActivity getActivity() {
        List<CommodityActivity> fields = ImmutableList.copyOf(getCommodity().getCommodityActivitiesSaved());
        System.out.printf("Number of items %d%n", fields.size());
        Collection<CommodityActivity> filteredFields = filter(fields, new Predicate<CommodityActivity>() {
            @Override
            public boolean apply(CommodityActivity input) {
                String testString = input.getActivityType().toLowerCase();
                System.out.printf("comparing %s to %s %n", testString, DISPENSE);
                return testString.contains(DISPENSE);
            }
        });

        return new ArrayList<>(filteredFields).get(0);
    }

    @Override
    public int getValue() {
        return quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setDispensing(Dispensing dispensing) {
        this.dispensing = dispensing;
    }
}
