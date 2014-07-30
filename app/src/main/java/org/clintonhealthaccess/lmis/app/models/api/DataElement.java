package org.clintonhealthaccess.lmis.app.models.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataElement {
    private String id;
    private String name;
    private CategoryCombo categoryCombo;
    private List<DataElementGroup> dataElementGroups;
}
