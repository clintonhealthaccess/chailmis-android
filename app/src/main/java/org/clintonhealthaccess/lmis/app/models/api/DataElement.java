package org.clintonhealthaccess.lmis.app.models.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

@Getter
@Setter
@Builder
public class DataElement {
    private String id;
    private String name;
    private CategoryCombo categoryCombo;
    private List<DataElementGroup> dataElementGroups;
}
