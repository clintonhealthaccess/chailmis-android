package org.clintonhealthaccess.lmis.app.models.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataElementGroupSet {

    private String name;
    private String created;
    private String lastUpdated;
    private String shortName;
    private String description;
    private Boolean compulsory;
    private Boolean dataDimension;
    private String href;
    private String id;
    private List<DataElementGroup> dataElementGroups = new ArrayList<DataElementGroup>();
}
