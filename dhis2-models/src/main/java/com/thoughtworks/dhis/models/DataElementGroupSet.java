package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class DataElementGroupSet {

    private String lastUpdated;

    private String id;

    private String created;

    private String name;

    private String shortName;

    private String href;

    private Boolean dataDimension;

    private String dimension;

    private String description;

    private Boolean externalAccess;

    private String displayName;

    private Boolean compulsory;


    private List<DataElementGroup> dataElementGroups = new ArrayList<DataElementGroup>();


    private List<Object> userGroupAccesses = new ArrayList<Object>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataElementGroupSet)) return false;

        DataElementGroupSet groupSet = (DataElementGroupSet) o;

        if (id != null ? !id.equals(groupSet.id) : groupSet.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
