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

import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderItemTest {

    @Test
    public void shouldCreateCommodityActionForAmountAndReason() throws Exception {

        Commodity commodity = mock(Commodity.class);
        CommodityAction amountActivity = new CommodityAction(commodity, "12", "12", OrderItem.ORDERED_AMOUNT);
        CommodityAction reasonActivity = new CommodityAction(commodity, "12", "demand", OrderItem.ORDER_REASON);
        CommodityAction emergencyOrder = new CommodityAction(commodity, "12", "demand", DataElementType.EMERGENCY_REASON_FOR_ORDER.getActivity());
        CommodityAction emergencyReason = new CommodityAction(commodity, "12", "demand", DataElementType.EMERGENCY_ORDERED_AMOUNT.getActivity());
        when(commodity.getCommodityAction(DataElementType.EMERGENCY_ORDERED_AMOUNT.getActivity())).thenReturn(emergencyOrder);
        when(commodity.getCommodityAction(DataElementType.EMERGENCY_REASON_FOR_ORDER.getActivity())).thenReturn(emergencyReason);
        when(commodity.getCommodityAction(DataElementType.ORDERED_AMOUNT.getActivity())).thenReturn(amountActivity);
        when(commodity.getCommodityAction(DataElementType.REASON_FOR_ORDER.getActivity())).thenReturn(reasonActivity);
        OrderCommodityViewModel commodityViewModel = new OrderCommodityViewModel(commodity, 10);
        String testReason = "reason";
        commodityViewModel.setReasonForUnexpectedOrderQuantity(new OrderReason(testReason));
        Order order = new Order();
        order.setOrderType(new OrderType(OrderType.ROUTINE));
        OrderItem item = new OrderItem(commodityViewModel);
        item.setOrder(order);
        assertThat(item.getActivitiesValues().size(), is(2));
        assertThat(item.getActivitiesValues().get(0).getValue(), is("10"));
        assertThat(item.getActivitiesValues().get(1).getValue(), is(testReason));
        assertThat(item.getActivitiesValues().get(0).getCommodityAction(), is(amountActivity));
        assertThat(item.getActivitiesValues().get(1).getCommodityAction(), is(reasonActivity));
    }

    @Test
    public void orderItemShouldCreateCommodityActionSnapShotValueForEmergencyOrder() throws Exception {
        Commodity commodity = mock(Commodity.class);
        CommodityAction commodityActionRoutineAmountOrdered = new CommodityAction(commodity, "12", "12", OrderItem.ORDERED_AMOUNT);
        CommodityAction reasonActivity = new CommodityAction(commodity, "12", "demand", OrderItem.ORDER_REASON);
        CommodityAction emergencyOrder = new CommodityAction(commodity, "12", "demand", DataElementType.EMERGENCY_REASON_FOR_ORDER.getActivity());
        CommodityAction emergencyReason = new CommodityAction(commodity, "12", "demand", DataElementType.EMERGENCY_ORDERED_AMOUNT.getActivity());
        when(commodity.getCommodityAction(DataElementType.EMERGENCY_ORDERED_AMOUNT.getActivity())).thenReturn(emergencyOrder);
        when(commodity.getCommodityAction(DataElementType.EMERGENCY_REASON_FOR_ORDER.getActivity())).thenReturn(emergencyReason);
        when(commodity.getCommodityAction(DataElementType.ORDERED_AMOUNT.getActivity())).thenReturn(commodityActionRoutineAmountOrdered);
        when(commodity.getCommodityAction(DataElementType.REASON_FOR_ORDER.getActivity())).thenReturn(reasonActivity);
        OrderCommodityViewModel commodityViewModel = new OrderCommodityViewModel(commodity, 10);
        String testReason = "reason";
        commodityViewModel.setReasonForUnexpectedOrderQuantity(new OrderReason(testReason));
        Order order = new Order();
        order.setOrderType(new OrderType(OrderType.EMERGENCY));
        OrderItem item = new OrderItem(commodityViewModel);
        item.setOrder(order);
        assertThat(item.getActivitiesValues().size(), is(2));
        assertThat(item.getActivitiesValues().get(0).getValue(), is("10"));
        assertThat(item.getActivitiesValues().get(1).getValue(), is(testReason));
        assertThat(item.getActivitiesValues().get(0).getCommodityAction(), is(emergencyOrder));
        assertThat(item.getActivitiesValues().get(1).getCommodityAction(), is(emergencyReason));

    }
}
