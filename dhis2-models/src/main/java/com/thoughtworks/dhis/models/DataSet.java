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
}
