
package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DataElement {

    private String id;

    private String created;

    private String name;

    private String href;

    private String lastUpdated;

    private String code;

    private String shortName;

    private Boolean zeroIsSignificant;

    private String type;

    private Boolean externalAccess;

    private String aggregationOperator;

    private String url;

    private String numberType;

    private String domainType;

    private String dimension;

    private String displayName;

    private CategoryCombo categoryCombo;

    private List<Object> aggregationLevels = new ArrayList<Object>();

    private List<DataElementGroup> dataElementGroups = new ArrayList<DataElementGroup>();

    private List<DataSet> dataSets = new ArrayList<DataSet>();

    private List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

    private List<Object> items = new ArrayList<Object>();

    private List<Object> userGroupAccesses = new ArrayList<Object>();

    private OptionSet optionSet;

}
