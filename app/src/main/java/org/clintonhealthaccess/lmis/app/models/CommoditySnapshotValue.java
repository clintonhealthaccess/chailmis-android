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

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static java.lang.String.valueOf;

@ToString
public class CommoditySnapshotValue {
    private CommodityAction commodityAction;
    private String value;
    private Date periodDate;

    public CommoditySnapshotValue(CommodityAction input, int quantity) {
        this(input, quantity, new Date());
    }

    public CommoditySnapshotValue(CommodityAction input, int quantity, Date periodDate) {
        this(input, valueOf(quantity), periodDate);
    }

    public CommoditySnapshotValue(CommodityAction input, String value) {
        this.commodityAction = input;
        this.value = value;
        this.periodDate = new Date();
    }

    public CommoditySnapshotValue(CommodityAction commodityAction, String value, Date periodDate) {
        this.commodityAction = commodityAction;
        this.value = value;
        this.periodDate = periodDate;
    }

    public Date getPeriodDate() {
        return periodDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommoditySnapshotValue)) return false;

        CommoditySnapshotValue value1 = (CommoditySnapshotValue) o;

        if (!commodityAction.equals(value1.commodityAction)) return false;
        if (!value.equals(value1.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = commodityAction.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    public CommodityAction getCommodityAction() {
        return commodityAction;
    }

    public String getValue() {
        return value;
    }
}

