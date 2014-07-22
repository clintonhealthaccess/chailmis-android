package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class OrderItem {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, foreign = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false)
    private Date startDate;

    @DatabaseField(canBeNull = false)
    private Date endDate;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false, foreign = true)
    private OrderReason reasonForOrder;

    @DatabaseField(foreign = true)
    private OrderReason reasonForUnexpectedQuantity;

    public OrderItem() {}
}
