package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.OrderReason;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class CommodityViewModel implements Serializable {
    private boolean selected;
    private int quantityEntered;
    private Commodity commodity;
    private int quantityPopulated;

    private Integer orderReasonPosition;
    private Date orderPeriodStartDate, orderPeriodEndDate;

    public CommodityViewModel(Commodity commodity) {
        this.commodity = commodity;
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

    public int getOrderDuration(){
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
        return (quantityEntered > 1.1 * this.quantityPopulated);
    }

    public int getQuantityPopulated() {
        return quantityPopulated;
    }

    public void setQuantityPopulated(int quantityPopulated) {
        this.quantityPopulated = quantityPopulated;
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
}
