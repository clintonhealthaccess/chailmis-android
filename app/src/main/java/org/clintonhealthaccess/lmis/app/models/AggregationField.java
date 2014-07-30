package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "aggregationFields")
public class AggregationField {
    @DatabaseField
    private String name;
    @DatabaseField(id = true, uniqueIndex = true)
    private String id;
    @DatabaseField(foreign = true)
    private Aggregation aggregation;
}
