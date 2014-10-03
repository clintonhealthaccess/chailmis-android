package org.clintonhealthaccess.lmis.app.sms;

import android.telephony.SmsManager;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.api.DataValueSet;

import java.util.List;

import roboguice.inject.InjectResource;

public class SmsSyncService {
    @InjectResource(R.string.dhis2_sms_number)
    private String dhis2SmsNumber;

    @Inject
    private SmsManagerFactory smsManagerFactory;

    public void send(DataValueSet dataValueSet) {
        SmsManager smsManager = smsManagerFactory.build();
        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);
        for (SmsValueSet smsValueSet : smsValueSets) {
            smsManager.sendTextMessage(dhis2SmsNumber, null, smsValueSet.toString(), null, null);
        }
    }
}
