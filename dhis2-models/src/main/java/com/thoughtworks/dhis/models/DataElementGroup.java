package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.List;

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
}
