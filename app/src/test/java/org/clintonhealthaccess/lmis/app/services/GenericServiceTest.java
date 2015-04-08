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

import org.clintonhealthaccess.lmis.LmisTestClass;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.models.LossItem;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import static org.clintonhealthaccess.lmis.app.services.GenericService.getTotal;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.dispense;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.lose;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.order;
import static org.clintonhealthaccess.lmis.utils.LMISTestCase.receive;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class GenericServiceTest extends LmisTestClass {

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
        receive(commodity, 20, receiveService);
        receive(commodity, 30, receiveService);

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
        receive(commodity, 200, receiveService);
        receive(commodity, 30, receiveService);

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

        dispense(commodity, 2, dispensingService);
        dispense(commodity, 3, dispensingService);

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

        dispense(commodity, 1, dispensingService);
        dispense(commodity, 1, dispensingService);

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

        lose(commodity, 3, lossService);
        lose(commodity, 2, lossService);

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

        order(commodity, 1, orderService);
        order(commodity, 1, orderService);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date endDate = calendar.getTime();

        int totalOrdered = GenericService.getTotal(commodity, startDate, endDate,
                Order.class, OrderItem.class, application);
        assertThat(totalOrdered, is(2));
    }
}