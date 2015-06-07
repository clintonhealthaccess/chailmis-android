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

package org.clintonhealthaccess.lmis.app.sms;

import android.content.SharedPreferences;
import android.telephony.SmsManager;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.thoughtworks.dhis.models.DataValue;
import com.thoughtworks.dhis.models.DataValueSet;

import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.sms.SmsSyncService.SMS_GATEWAY_NUMBER;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SmsSyncServiceTest {
    @Inject
    private SmsSyncService smsSyncService;

    @Inject
    SharedPreferences sharedPreferences;

    private SmsManager mockSmsManager;
    private UserService mockUserService;
    private LmisServer mockLmisServer;

    @Before
    public void setUp() throws Exception {
        mockSmsManager = mock(SmsManager.class);
        mockUserService = mock(UserService.class);
        mockLmisServer = mock(LmisServer.class);

        final SmsManagerFactory mockSmsManagerFactory = mock(SmsManagerFactory.class);
        when(mockSmsManagerFactory.build()).thenReturn(mockSmsManager);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SmsManagerFactory.class).toInstance(mockSmsManagerFactory);
                bind(UserService.class).toInstance(mockUserService);
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        });
    }

    @Test
    public void shouldSendDataValueSetThroughSms() throws Exception {
        DataValue dataValue1 = DataValue.builder().dataSet("set_1").dataElement("element_1").period("20141005").value("11").build();
        DataValue dataValue2 = DataValue.builder().dataSet("set_1").dataElement("element_2").period("20141005").value("22").build();
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataValues(newArrayList(dataValue1, dataValue2));

        smsSyncService.send(dataValueSet);

        String defaultDhis2SmsNumber = smsSyncService.getDefaultDhis2SmsNumber();
        verify(mockSmsManager).sendTextMessage(defaultDhis2SmsNumber, null, "set_1 0510 element_1.11.element_2.22", null, null);
    }

    @Test
    public void shouldSyncSmsGatewayNumberFromLmisServer() throws Exception {
        String expectedGatewayNumber = "+256785111222";
        String defaultDhis2SmsNumber = smsSyncService.getDefaultDhis2SmsNumber();

        User user = new User();
        when(mockUserService.getRegisteredUser()).thenReturn(user);
        when(mockLmisServer.fetchPhoneNumberConstant(user, SMS_GATEWAY_NUMBER, defaultDhis2SmsNumber)).thenReturn(expectedGatewayNumber);

        assertThat(sharedPreferences.getString(SMS_GATEWAY_NUMBER, null), nullValue());

        smsSyncService.syncGatewayNumber();

        assertThat(sharedPreferences.getString(SMS_GATEWAY_NUMBER, null), is(expectedGatewayNumber));
    }
}