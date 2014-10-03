package com.thoughtworks.dhis.models;

import lombok.Data;

import java.util.List;

@Data
public class DataValueSet {
    List<DataValue> dataValues;
    private String dataSet, orgUnit, period;
}
