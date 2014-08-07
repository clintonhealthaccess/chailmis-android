package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "receives")
public class Receive implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<ReceiveItem> receiveItemsCollection;

    private List<ReceiveItem> receiveItems = new ArrayList<>();


    public void addReceiveItem(ReceiveItem receiveItem) {
        receiveItem.setReceive(this);
        this.receiveItems.add(receiveItem);
    }
}
