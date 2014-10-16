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

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.Date;

import static org.clintonhealthaccess.lmis.app.services.GenericService.getTotal;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class GenericServiceTest {

    @Inject
    ReceiveService receiveService;
    @Inject
    CommodityService commodityService;
    @Inject
    DispensingService dispensingService;
    @Inject
    LossService lossService;
    @Inject
    OrderService orderService;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
    }

    @Test
    public void shouldReturnTotalQuantityReceived() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        createReceive(commodity, 20);
        createReceive(commodity, 30);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalReceived = getTotal(commodity, startingDate, endDate,
                Receive.class, ReceiveItem.class, application);
        assertThat(totalReceived, is(50));

    }

    @Test
    public void shouldReturnCorrectTotalQuantityReceived() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        createReceive(commodity, 200);
        createReceive(commodity, 30);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalReceived = getTotal(commodity, startingDate, endDate,
                Receive.class, ReceiveItem.class, application);
        assertThat(totalReceived, is(230));

    }

    @Test
    public void shouldReturnTotalQuantityDispensed() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        dispense(commodity, 2);
        dispense(commodity, 3);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalDispensed = GenericService.getTotal(commodity, startingDate, endDate,
                Dispensing.class, DispensingItem.class, application);
        assertThat(totalDispensed, is(5));

    }

    
    @Test
    public void shouldReturnCorrectTotalQuantityDispensed() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        dispense(commodity, 1);
        dispense(commodity, 1);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startingDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalDispensed = GenericService.getTotal(commodity, startingDate, endDate,
                Dispensing.class, DispensingItem.class, application);
        assertThat(totalDispensed, is(2));

    }

    @Test
     public void shouldReturnValidQuantityLost() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        createLoss(commodity, 3);
        createLoss(commodity, 2);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalLost = GenericService.getTotal(commodity, startDate, endDate,
                Loss.class, LossItem.class, application);
        assertThat(totalLost, is(5));
    }

    @Test
    public void shouldReturnValidQuantityOrdered() throws Exception {
        Commodity commodity = commodityService.all().get(0);

        createOrder(commodity, 1);
        createOrder(commodity, 1);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalOrdered = GenericService.getTotal(commodity, startDate, endDate,
                Order.class, OrderItem.class, application);
        assertThat(totalOrdered, is(2));
    }

    private void createOrder(Commodity commodity, int quantity) {
        Order order = new Order();
        order.setSrvNumber("TEST");
        order.setOrderType(new OrderType(OrderType.ROUTINE));

        OrderCommodityViewModel orderCommodityViewModel = new OrderCommodityViewModel(commodity, quantity);
        orderCommodityViewModel.setOrderPeriodStartDate(new Date());
        orderCommodityViewModel.setOrderPeriodEndDate(new Date());

        OrderItem orderItem = new OrderItem(orderCommodityViewModel);
        order.addItem(orderItem);

        orderService.saveOrder(order);
    }

    private void createLoss(Commodity commodity, int quantityLost) {
        Loss loss = new Loss();
        LossItem lossItem = new LossItem(commodity, quantityLost);
        loss.addLossItem(lossItem);
        lossService.saveLoss(loss);
    }

    private void dispense(Commodity commodity, int quantity) {
        Dispensing dispensing = new Dispensing();
        DispensingItem dispensingItem = new DispensingItem(commodity, quantity);
        dispensing.addItem(dispensingItem);
        dispensingService.addDispensing(dispensing);
    }

    private void createReceive(Commodity commodity, int quantityReceived) {
        Receive receive = new Receive("LGA");

        ReceiveItem receiveItem = new ReceiveItem();
        receiveItem.setCommodity(commodity);
        receiveItem.setQuantityAllocated(quantityReceived);
        receiveItem.setQuantityReceived(quantityReceived);

        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);
    }




}