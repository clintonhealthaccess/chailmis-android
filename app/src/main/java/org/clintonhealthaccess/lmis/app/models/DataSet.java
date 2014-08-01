package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "datasets")
public class DataSet {

    @DatabaseField
    private String description;
    @DatabaseField
    private String id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String periodType;
    @DatabaseField(generatedId = true)
    private Long dataSetId;
}
