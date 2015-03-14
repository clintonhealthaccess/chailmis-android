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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

@Getter
@Setter
@Builder
public class DataElementGroup {

    private String lastUpdated;

    private String id;

    private String created;

    private String name;

    private String shortName;

    private String href;

    private String publicAccess;

    private Boolean externalAccess;

    private String displayName;

    private User user;

    private DataElementGroupSet dataElementGroupSet;

    private List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

    private List<DataElement> dataElements = new ArrayList<DataElement>();

    private List<Object> userGroupAccesses = new ArrayList<Object>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataElementGroup group = (DataElementGroup) o;

        if (!name.equals(group.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String jsonString() {
        return "\"id\": \'" + id + "\', \"name\": \'" + name + "\', " +
                "\"attributeValues\": [" + attributeValuesJsonString() + "]," +
                "\"dataElements\": [" + dataElementsJsonString() + "]";
    }

    private String attributeValuesJsonString() {
        String s = "";
        if (attributeValues != null) {
            for (AttributeValue t : attributeValues) {
                if (!s.equalsIgnoreCase("")) {
                    s += ",";
                }
                s += "{" + t.jsonString() + "}";
            }
        }
        return s;
    }

    private String dataElementsJsonString() {
        String s = "";
        for (DataElement e : dataElements) {
            if (!s.equalsIgnoreCase("")) {
                s += ",";
            }
            s += "{" + e.jsonString() + "}";
        }
        return s;
    }

    @Override
    public String toString() {
        return name + "  " + id + "  " +
                (dataElements != null ? dataElements.size() : "NO") + " Elements";
    }
}
