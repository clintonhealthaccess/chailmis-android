package org.clintonhealthaccess.lmis.app.models;

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable
public class Allocation {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String allocationId;

    @DatabaseField(canBeNull = false)
    private boolean received;


    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<AllocationItem> allocationItems;

    public List<AllocationItem> getAllocationItems() {
        return ImmutableList.copyOf(allocationItems);
    }
}
