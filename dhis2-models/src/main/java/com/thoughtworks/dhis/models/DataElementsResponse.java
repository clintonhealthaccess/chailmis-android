
package com.thoughtworks.dhis.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataElementsResponse {

    private Pager pager;
    private List<DataElement> dataElements = new ArrayList<DataElement>();

}
