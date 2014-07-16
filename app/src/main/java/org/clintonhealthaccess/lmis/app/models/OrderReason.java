package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "orderReasons")
public class OrderReason {

    public static final String ORDER_REASONS_JSON_KEY = "order_reasons";
    public static final String UNEXPECTED_QUANTITY_JSON_KEY = "unexpected_quantity_reasons";

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String reason;

    @DatabaseField(canBeNull = false)
    private String type;

    public OrderReason() {
        //Orm lite wants it
    }

    public OrderReason(String reason, String type) {
        this.reason = reason;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderReason that = (OrderReason) o;

        return reason.equals(that.reason) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = reason.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }
}
