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

package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@Builder
public class DataSet {

    private String id;

    private String created;

    private String name;

    private String href;

    private String lastUpdated;

    private String shortName;

    private Integer expiryDays;

    private Integer version;

    private Boolean approveData;

    private Boolean renderHorizontally;

    private Boolean externalAccess;

    private Boolean fieldCombinationRequired;

    private Boolean skipOffline;

    private Boolean skipAggregation;

    private Boolean validCompleteOnly;

    private String publicAccess;

    private Boolean noValueRequiresComment;

    private Boolean notifyCompletingUser;

    private Integer timelyDays;

    private Boolean renderAsTabs;

    private Boolean allowFuturePeriods;

    private Boolean dataElementDecoration;

    private String periodType;

    private String displayName;

    private Boolean mobile;

    private CategoryCombo categoryCombo;

    private User user;

    private List<Object> organisationUnitGroups = new ArrayList<Object>();

    private List<Object> sections = new ArrayList<Object>();

    private List<DataElement> dataElements = new ArrayList<DataElement>();

    private List<Object> organisationUnits = new ArrayList<Object>();

    private List<Indicator> indicators = new ArrayList<Indicator>();

    private List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

    private List<Object> compulsoryDataElementOperands = new ArrayList<Object>();

    private List<Object> userGroupAccesses = new ArrayList<Object>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSet dataSet = (DataSet) o;

        if (name != null ? !name.equals(dataSet.name) : dataSet.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name + "  " + id + "  " +
                (dataElements != null ? dataElements.size() : "NO") + " Elements";
    }
}
