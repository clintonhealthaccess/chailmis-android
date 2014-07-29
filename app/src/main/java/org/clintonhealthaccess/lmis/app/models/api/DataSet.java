package org.clintonhealthaccess.lmis.app.models.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataSet {

    private List<DataElement> dataElements = new ArrayList<DataElement>();
    private String description;
    private String id;
    private String name;

}
