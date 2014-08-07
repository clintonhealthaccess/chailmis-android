package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable
public class AllocationItem {

    @DatabaseField(foreign = true, canBeNull = false)
    private Allocation allocation;

    @DatabaseField(foreign = true, canBeNull = false)
    private Commodity commodity;

    @DatabaseField(canBeNull = false)
    private int quantity;
}
