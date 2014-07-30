package org.clintonhealthaccess.lmis.app.models;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "aggregations")
public class Aggregation {
    @DatabaseField
    private String name;
    @DatabaseField(id = true, uniqueIndex = true)
    private String id;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<AggregationField> aggregationFieldsCollection;

    @SerializedName("categoryOptionCombos")
    private List<AggregationField> aggregationFields;
}
