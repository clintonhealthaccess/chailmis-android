package org.clintonhealthaccess.lmis.app.sms;

import android.telephony.SmsManager;

import com.google.inject.Inject;
import com.thoughtworks.dhis.models.DataValueSet;

import org.clintonhealthaccess.lmis.app.R;

import java.util.List;

import roboguice.inject.InjectResource;

import static android.util.Log.e;
import static android.util.Log.i;
import static java.lang.String.format;

public class SmsSyncService {
    @InjectResource(R.string.dhis2_sms_number)
    private String dhis2SmsNumber;

    @Inject
    private SmsManagerFactory smsManagerFactory;

    public boolean send(DataValueSet dataValueSet) {
        SmsManager smsManager = smsManagerFactory.build();
        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);
        i("SMS Sync", format("Start syncing through SMS, %d messages in total", smsValueSets.size()));
        for (SmsValueSet smsValueSet : smsValueSets) {
            try {
                i("SMS Sync", format("Sending to [%s] ==> %s", dhis2SmsNumber, smsValueSet));
                smsManager.sendTextMessage(dhis2SmsNumber, null, smsValueSet.toString(), null, null);
            } catch(Exception e) {
                e("SMS Sync", format("Failed to send smsValueSet: %s", smsValueSet), e);
                return false;
            }
        }
        return true;
    }
}
