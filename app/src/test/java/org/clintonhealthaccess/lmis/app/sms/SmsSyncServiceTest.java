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

        verify(mockSmsManager).sendTextMessage("+256785000000", null, "set_1 0510 element_1.11.element_2.22", null, null);
    }

    @Test
    public void shouldSyncSmsGatewayNumberFromLmisServer() throws Exception {
        String expectedGatewayNumber = "+256785111222";
        User user = new User();
        when(mockUserService.getRegisteredUser()).thenReturn(user);
        when(mockLmisServer.fetchPhoneNumberConstant(user, SMS_GATEWAY_NUMBER, "+256785000000")).thenReturn(expectedGatewayNumber);

        assertThat(sharedPreferences.getString(SMS_GATEWAY_NUMBER, null), nullValue());

        smsSyncService.syncGatewayNumber();

        assertThat(sharedPreferences.getString(SMS_GATEWAY_NUMBER, null), is(expectedGatewayNumber));
    }
}