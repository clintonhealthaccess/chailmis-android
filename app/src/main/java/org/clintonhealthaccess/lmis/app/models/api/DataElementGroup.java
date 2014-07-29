package org.clintonhealthaccess.lmis.app.models.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;


@Getter
@Setter
@Builder
public class DataElementGroup {

    private String name;
    private String created;
    private String lastUpdated;
    private String href;
    private String id;
    private List<DataElement> dataElements = new ArrayList<>();

}
