package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;

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

    @DatabaseField(foreign = true, canBeNull = false)
    private Order order;

    @DatabaseField(canBeNull = false, foreign = true)
    private OrderReason reasonForOrder;

    @DatabaseField(foreign = true)
    private OrderReason reasonForUnexpectedQuantity;

    public OrderItem() {}

    public OrderItem(CommodityViewModel commodityViewModel) {
        this.commodity = commodityViewModel.getCommodity();
        this.startDate = commodityViewModel.getOrderPeriodStartDate();
        this.endDate = commodityViewModel.getOrderPeriodEndDate();
        this.quantity = commodityViewModel.getQuantityEntered();
        this.reasonForOrder = commodityViewModel.getReasonForOrder();
        this.reasonForUnexpectedQuantity = commodityViewModel.getReasonForUnexpectedOrderQuantity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItem orderItem = (OrderItem) o;

        if (quantity != orderItem.quantity) return false;
        if (commodity != null ? !commodity.equals(orderItem.commodity) : orderItem.commodity != null)
            return false;
        if (endDate != null ? !endDate.equals(orderItem.endDate) : orderItem.endDate != null)
            return false;
        if (reasonForOrder != null ? !reasonForOrder.equals(orderItem.reasonForOrder) : orderItem.reasonForOrder != null)
            return false;
        if (reasonForUnexpectedQuantity != null ? !reasonForUnexpectedQuantity.equals(orderItem.reasonForUnexpectedQuantity) : orderItem.reasonForUnexpectedQuantity != null)
            return false;
        if (startDate != null ? !startDate.equals(orderItem.startDate) : orderItem.startDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = commodity != null ? commodity.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + quantity;
        result = 31 * result + (reasonForOrder != null ? reasonForOrder.hashCode() : 0);
        result = 31 * result + (reasonForUnexpectedQuantity != null ? reasonForUnexpectedQuantity.hashCode() : 0);
        return result;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public String getLmisId() {
        return commodity.getLmisId();
    }
}
