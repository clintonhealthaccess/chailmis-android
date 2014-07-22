package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class Order {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    private String srvNumber = "DUMMY ORDER ID NUMBER";

    @ForeignCollectionField(eager = true)
    private ForeignCollection<OrderItem> orderItems;

    public Order() {}

}
