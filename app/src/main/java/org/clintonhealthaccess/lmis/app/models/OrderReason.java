package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "orderReasons")
public class OrderReason {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String reason;

    public OrderReason() {
        //Orm lite
    }

    public OrderReason(String reason) {
        this.reason = reason;
    }
}
