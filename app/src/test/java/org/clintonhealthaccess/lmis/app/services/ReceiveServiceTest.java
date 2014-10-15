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

import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.clintonhealthaccess.lmis.app.services.GenericService.getTotal;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveServiceTest {
    @Inject
    CommodityService commodityService;

    @Inject
    ReceiveService receiveService;

    public static final int QUANTITY_ALLOCATED = 4;
    public static final int QUANTITY_RECEIVED = 3;
    private GenericDao<Receive> receiveDao;
    private GenericDao<Allocation> allocationsDao;

    @Before
    public void setUp() throws Exception {
        setUpInjectionWithMockLmisServer(application, this);
        commodityService.initialise(new User("test", "pass"));
        receiveDao = new GenericDao<>(Receive.class, Robolectric.application);
        allocationsDao = new GenericDao<>(Allocation.class, Robolectric.application);
    }

    @Test
    public void shouldSaveReceiveAndReceiveItems() {
        Commodity commodity = commodityService.all().get(0);
        ReceiveItem receiveItem = new ReceiveItem(commodity, QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        Receive receive = new Receive(true, null);
        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);

        assertThat(receiveDao.queryForAll().size(), is(1));
        assertThat(receiveDao.queryForAll().get(0).getReceiveItemsCollection().size(), is(1));
    }

    @Test
    public void shouldUpdateCommodityStockOnHandOnSaveReceive() throws Exception {
        Commodity commodity = commodityService.all().get(0);
        int newStockOnHand = commodity.getStockOnHand() + QUANTITY_RECEIVED;

        ReceiveItem receiveItem = new ReceiveItem(commodity, QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        Receive receive = new Receive(true, null);
        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);
        commodity = commodityService.all().get(0);

        assertThat(commodity.getStockOnHand(), is(newStockOnHand));
    }

    @Test
    public void shouldMarkAllocationAsReceivedIfHasAllocation() throws Exception {

        Commodity commodity = commodityService.all().get(0);
        ReceiveItem receiveItem = new ReceiveItem(commodity, QUANTITY_ALLOCATED, QUANTITY_RECEIVED);
        Receive receive = new Receive();

        Allocation allocation = new Allocation("UG-002", "20140901");
        allocation.setReceived(false);
        allocation = allocationsDao.create(allocation);

        receive.setAllocation(allocation);
        receive.addReceiveItem(receiveItem);

        receiveService.saveReceive(receive);

        assertThat(receiveDao.queryForAll().size(), is(1));
        assertThat(receiveDao.queryForAll().get(0).getReceiveItemsCollection().size(), is(1));
        assertThat(allocationsDao.getById(String.valueOf(allocation.getId())).isReceived(), is(true));
    }




}