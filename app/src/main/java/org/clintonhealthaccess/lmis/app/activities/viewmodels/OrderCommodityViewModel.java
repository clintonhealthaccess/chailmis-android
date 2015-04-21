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

package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.OrderCycle;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.utils.Helpers;

import java.util.Date;

public class OrderCommodityViewModel extends BaseCommodityViewModel {
    public int getExpectedOrderQuantity() {
        return expectedOrderQuantity;
    }

    private int expectedOrderQuantity;
    private Integer orderReasonPosition;
    private Date orderPeriodStartDate, orderPeriodEndDate;
    private int unexpectedReasonPosition;
    private OrderReason reasonForUnexpectedOrderQuantity;

    public OrderCommodityViewModel(Commodity commodity) {
        super(commodity);
    }

    public OrderCommodityViewModel(Commodity commodity, int quantity) {
        super(commodity, quantity);
    }

    public boolean quantityIsUnexpected() {
        return (quantityEntered > (1.1 * getExpectedOrderQuantity()));
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


    public OrderReason getReasonForUnexpectedOrderQuantity() {
        return reasonForUnexpectedOrderQuantity;
    }

    public void setReasonForUnexpectedOrderQuantity(OrderReason reasonForUnexpectedOrderQuantity) {
        this.reasonForUnexpectedOrderQuantity = reasonForUnexpectedOrderQuantity;
    }

    public boolean isValidAsOrderItem() {
        return orderPeriodEndDate != null && orderPeriodStartDate != null && quantityEntered > 0;
    }

    public boolean isUnexpectedReasonsSpinnerVisible(String dateText, Date actualDate, boolean typeIsRoutine) {
        if (!typeIsRoutine) {
            return true;
        }

        if (actualDate != null && dateText != null && typeIsRoutine) {
            if (!dateText.equalsIgnoreCase(SelectedOrderCommoditiesAdapter.SIMPLE_DATE_FORMAT.format(actualDate))) {
                return true;
            }
        }

        return this.quantityIsUnexpected();
    }

    public Date getExpectedStartDate() {
        return getOrderCycle().startDate(new Date());
    }

    public Date getExpectedEndDate() {
        return getOrderCycle().endDate(new Date());
    }

    private OrderCycle getOrderCycle() {
        String orderFrequency = getCommodity().getOrderFrequency();
        OrderCycle orderCycle;
        orderCycle = Helpers.getOrderCycle(orderFrequency);
        return orderCycle;
    }

    public int getSuggestedAmount() {
        int suggestedAmount = getCommodity().getMaximumThreshold() - getStockOnHand();
        return suggestedAmount < 0 ? 0 : suggestedAmount;
    }
}
