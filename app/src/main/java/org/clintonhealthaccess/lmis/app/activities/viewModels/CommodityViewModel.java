package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.OrderReason;

import java.io.Serializable;
import java.util.Date;

public class CommodityViewModel implements Serializable {
    private boolean selected;
    private int quantityEntered;
    private Commodity commodity;
    private int expectedOrderQuantity;

    private Integer orderReasonPosition;
    private Date orderPeriodStartDate, orderPeriodEndDate;
    private int unexpectedReasonPosition;
    private OrderReason reasonForOrder;
    private OrderReason reasonForUnexpectedOrderQuantity;

    public CommodityViewModel(Commodity commodity) {
        this.commodity = commodity;
    }

    public CommodityViewModel(Commodity commodity, int quantityEntered) {
        this(commodity);
        this.quantityEntered = quantityEntered;
    }

    public boolean isSelected() {
        return selected;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public int getQuantityEntered() {
        return quantityEntered;
    }

    public void setQuantityEntered(int quantityEntered) {
        this.quantityEntered = quantityEntered;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public String getName() {
        return commodity.getName();
    }

    public boolean stockIsFinished() {
        return commodity.stockIsFinished();
    }

    public int getOrderDuration() {
        return commodity.getOrderDuration();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommodityViewModel that = (CommodityViewModel) o;

        return commodity.equals(that.commodity);
    }

    @Override
    public int hashCode() {
        return commodity.hashCode();
    }

    public boolean quantityIsUnexpected() {
        return (quantityEntered > 1.1 * this.expectedOrderQuantity);
    }

    public boolean quantityIsValid() {
        return quantityEntered > 0;
    }

    public int getExpectedOrderQuantity() {
        return expectedOrderQuantity;
    }

    public void setExpectedOrderQuantity(int expectedOrderQuantity) {
        this.expectedOrderQuantity = expectedOrderQuantity;
    }

    public Date getOrderPeriodEndDate() {
        return orderPeriodEndDate;
    }

    public void setOrderPeriodEndDate(Date orderPeriodEndDate) {
        this.orderPeriodEndDate = orderPeriodEndDate;
    }

    public Date getOrderPeriodStartDate() {
        return orderPeriodStartDate;
    }

    public void setOrderPeriodStartDate(Date orderPeriodStartDate) {
        this.orderPeriodStartDate = orderPeriodStartDate;
    }

    public Integer getOrderReasonPosition() {
        return orderReasonPosition;
    }

    public void setOrderReasonPosition(Integer orderReasonPosition) {
        this.orderReasonPosition = orderReasonPosition;
    }

    public int getUnexpectedReasonPosition() {
        return unexpectedReasonPosition;
    }

    public void setUnexpectedReasonPosition(int unexpectedReasonPosition) {
        this.unexpectedReasonPosition = unexpectedReasonPosition;
    }

    public OrderReason getReasonForOrder() {
        return reasonForOrder;
    }

    public void setReasonForOrder(OrderReason reasonForOrder) {
        this.reasonForOrder = reasonForOrder;
    }

    public OrderReason getReasonForUnexpectedOrderQuantity() {
        return reasonForUnexpectedOrderQuantity;
    }

    public void setReasonForUnexpectedOrderQuantity(OrderReason reasonForUnexpectedOrderQuantity) {
        this.reasonForUnexpectedOrderQuantity = reasonForUnexpectedOrderQuantity;
    }
}
