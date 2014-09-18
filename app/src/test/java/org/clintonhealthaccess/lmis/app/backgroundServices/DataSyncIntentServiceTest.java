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

package org.clintonhealthaccess.lmis.app.backgroundServices;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class DataSyncIntentServiceTest {

    private UserService mockUserService;
    private CommodityService mockCommodityService;
    private OrderService mockOrderService;
    private StockService mockStockService;

    @Before
    public void setUp() throws Exception {
        mockUserService = mock(UserService.class);
        mockCommodityService = mock(CommodityService.class);
        mockStockService = mock(StockService.class);
        mockOrderService = mock(OrderService.class);

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
                bind(CommodityService.class).toInstance(mockCommodityService);
                bind(StockService.class).toInstance(mockStockService);
                bind(OrderService.class).toInstance(mockOrderService);
            }
        });

    }

    @Test
    public void shouldInitialiseCommodities() throws Exception {
        Intent serviceIntent = new Intent(Robolectric.application, DataSyncIntentService.class);
        DataSyncIntentService service = new DataSyncIntentService();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
        verify(mockCommodityService).initialise((org.clintonhealthaccess.lmis.app.models.User) anyObject());
    }

    @Test
    public void shouldSyncOrderReasons() throws Exception {
        Intent serviceIntent = new Intent(Robolectric.application, DataSyncIntentService.class);
        DataSyncIntentService service = new DataSyncIntentService();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
        verify(mockOrderService, atLeastOnce()).syncOrderReasons();
    }

    @Test
    public void shouldSyncOrderTypes() throws Exception {
        Intent serviceIntent = new Intent(Robolectric.application, DataSyncIntentService.class);
        DataSyncIntentService service = new DataSyncIntentService();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
        verify(mockOrderService, atLeastOnce()).syncOrderTypes();
    }

    @Test
    public void shouldCreateNotifications() throws Exception {
        Intent serviceIntent = new Intent(Robolectric.application, DataSyncIntentService.class);
        NotificationManager notificationManager = (NotificationManager) Robolectric.application.getSystemService(Context.NOTIFICATION_SERVICE);
        DataSyncIntentService service = new DataSyncIntentService();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
        assertThat(Robolectric.shadowOf(notificationManager).size(), is(1));
    }
}