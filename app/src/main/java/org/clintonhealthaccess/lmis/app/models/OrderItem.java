/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;

import java.util.Date;

public class OrderItem {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false)
    private Date startDate;

    @DatabaseField(canBeNull = false)
    private Date endDate;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(foreign = true, canBeNull = false)
    private Order order;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private OrderReason reasonForOrder;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private OrderReason reasonForUnexpectedQuantity;

    public OrderItem() {
        //Orm lite likes
    }

    public OrderItem(OrderCommodityViewModel commodityViewModel) {
        this.commodity = commodityViewModel.getCommodity();
        this.startDate = commodityViewModel.getOrderPeriodStartDate();
        this.endDate = commodityViewModel.getOrderPeriodEndDate();
        this.quantity = commodityViewModel.getQuantityEntered();
        this.reasonForOrder = commodityViewModel.getReasonForOrder();
        this.reasonForUnexpectedQuantity = commodityViewModel.getReasonForUnexpectedOrderQuantity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrderItem orderItem = (OrderItem) o;

        if (quantity != orderItem.quantity) {
            return false;
        }
        if (commodity != null ? !commodity.equals(orderItem.commodity) : orderItem.commodity != null) {
            return false;
        }
        if (endDate != null ? !endDate.equals(orderItem.endDate) : orderItem.endDate != null) {
            return false;
        }
        if (reasonForOrder != null ? !reasonForOrder.equals(orderItem.reasonForOrder) : orderItem.reasonForOrder != null) {
            return false;
        }
        if (reasonForUnexpectedQuantity != null ? !reasonForUnexpectedQuantity.equals(orderItem.reasonForUnexpectedQuantity) : orderItem.reasonForUnexpectedQuantity != null) {
            return false;
        }
        if (startDate != null ? !startDate.equals(orderItem.startDate) : orderItem.startDate != null) {
            return false;
        }

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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getCommodtyName() {
        return commodity.getName();
    }

    public String getSRVNumber() {
        return order.getSrvNumber();
    }

    public int getQuantity() {
        return quantity;
    }
}
