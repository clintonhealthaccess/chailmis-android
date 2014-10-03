package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Getter
@Setter
public class DataSetSearchResponse {
    private List<DataSet> dataSets = newArrayList();
}
