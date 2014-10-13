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

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.services.AlertsService;
import org.clintonhealthaccess.lmis.app.services.AllocationService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class AlertsGenerationIntentServiceTest {
    private AlertsService alertService;

    @Before
    public void setUp() throws Exception {
        alertService = mock(AlertsService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(AlertsService.class).toInstance(alertService);
            }
        });
        AllocationService.clearCache();
    }


    @Test
    public void shouldUpdateLowStockAlerts() throws Exception {
        AlertsGenerationIntentServiceMock alertsGenerationIntentService = new AlertsGenerationIntentServiceMock();
        alertsGenerationIntentService.onCreate();
        alertsGenerationIntentService.onHandleIntent(null);
        verify(alertService).updateLowStockAlerts();
    }

    @Test
    public void shouldGenerateRoutineOrderAlerts() throws Exception {
        AlertsGenerationIntentServiceMock alertsGenerationIntentService = new AlertsGenerationIntentServiceMock();
        alertsGenerationIntentService.onCreate();
        alertsGenerationIntentService.onHandleIntent(null);
        verify(alertService).generateRoutineOrderAlert((java.util.Date) any());
    }

    @Test
    public void shouldGenerateAllocationAlerts() throws Exception {
        AlertsGenerationIntentServiceMock alertsGenerationIntentService = new AlertsGenerationIntentServiceMock();
        alertsGenerationIntentService.onCreate();
        alertsGenerationIntentService.onHandleIntent(null);
        verify(alertService).generateAllocationAlerts();
    }

    @Test
    public void shouldGenerateMonthlyStockCountAlerts() throws Exception {
        AlertsGenerationIntentServiceMock alertsGenerationIntentService = new AlertsGenerationIntentServiceMock();
        alertsGenerationIntentService.onCreate();
        alertsGenerationIntentService.onHandleIntent(null);
        verify(alertService).generateMonthlyStockCountAlerts((java.util.Date) any());
    }

    class AlertsGenerationIntentServiceMock extends AlertsGenerationIntentService {
        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }
}