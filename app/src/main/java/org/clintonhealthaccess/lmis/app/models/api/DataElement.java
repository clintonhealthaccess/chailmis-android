package org.clintonhealthaccess.lmis.app.models.api;

import com.google.gson.annotations.SerializedName;

import org.clintonhealthaccess.lmis.app.models.Aggregation;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataElement {
    private String id;
    private String name;
    @SerializedName("categoryCombo")
    private Aggregation aggregation;
    private List<DataElementGroup> dataElementGroups;
}
