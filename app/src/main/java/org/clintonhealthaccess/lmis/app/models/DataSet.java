package org.clintonhealthaccess.lmis.app.models;

import java.util.ArrayList;
import java.util.List;

public class DataSet {

    private List<DataElement> dataElements = new ArrayList<DataElement>();
    private String description;
    private String id;
    private String name;


    public List<DataElement> getDataElements() {
        return dataElements;
    }

    public void setDataElements(List<DataElement> dataElements) {
        this.dataElements = dataElements;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
