package com.thoughtworks.dhis.models;

import java.util.List;

import lombok.Data;

import static com.google.common.collect.Lists.newArrayList;

@Data
public class DataValueSet {
    List<DataValue> dataValues = newArrayList();
    private String dataSet, orgUnit, period;
}
