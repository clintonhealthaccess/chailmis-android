package org.clintonhealthaccess.lmis.app.models.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DataElementGroup {

    private String name;
    private String created;
    private String lastUpdated;
    private String href;
    private String id;
    private List<DataElement> dataElements;

    public DataElementGroup() {
        dataElements = new ArrayList<>();
    }
}
