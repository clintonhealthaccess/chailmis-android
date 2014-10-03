package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class Attribute {
    private String id;
    private String created;
    private String name;
    private String href;
    private String lastUpdated;
    private String code;
    private Boolean organisationUnitGroupSetAttribute;
    private Boolean userGroupAttribute;
    private Boolean dataElementAttribute;
    private Boolean dataElementGroupAttribute;
    private Boolean externalAccess;
    private String valueType;
    private Boolean indicatorGroupAttribute;
    private Boolean organisationUnitAttribute;
    private Boolean mandatory;
    private Boolean dataSetAttribute;
    private Boolean indicatorAttribute;
    private Boolean userAttribute;

    private Boolean organisationUnitGroupAttribute;

    private String displayName;

}
