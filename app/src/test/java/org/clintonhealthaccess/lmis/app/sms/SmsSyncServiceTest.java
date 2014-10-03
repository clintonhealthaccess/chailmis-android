package org.clintonhealthaccess.lmis.app.sms;

import android.telephony.SmsManager;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.api.DataValue;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SmsSyncServiceTest {
    @Inject
    private SmsSyncService smsSyncService;

    private SmsManager mockSmsManager;

    @Before
    public void setUp() throws Exception {
        mockSmsManager = mock(SmsManager.class);

        final SmsManagerFactory mockSmsManagerFactory = mock(SmsManagerFactory.class);
        when(mockSmsManagerFactory.build()).thenReturn(mockSmsManager);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SmsManagerFactory.class).toInstance(mockSmsManagerFactory);
            }
        });
    }

    @Test
    public void shouldSendDataValueSetThroughSms() throws Exception {
        DataValue dataValue1 = DataValue.builder().dataElement("element_1").value("11").build();
        DataValue dataValue2 = DataValue.builder().dataElement("element_2").value("22").build();
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataSet("set_1");
        dataValueSet.setDataValues(newArrayList(dataValue1, dataValue2));

        smsSyncService.send(dataValueSet);

        verify(mockSmsManager).sendTextMessage("+256785000000", null, "set_1 element_1.11.element_2.22", null, null);
    }
}