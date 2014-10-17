package org.clintonhealthaccess.lmis.app.sms;

import android.content.SharedPreferences;
import android.telephony.SmsManager;

import com.google.inject.Inject;
import com.thoughtworks.dhis.models.DataValueSet;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.app.services.UserService;

import java.util.List;

import roboguice.inject.InjectResource;

import static android.util.Log.e;
import static android.util.Log.i;
import static java.lang.String.format;

public class SmsSyncService {
    static final String SMS_GATEWAY_NUMBER = "SMS_GATEWAY_NUMBER";

    @InjectResource(R.string.dhis2_sms_number)
    private String defaultDhis2SmsNumber;

    @Inject
    private UserService userService;

    @Inject
    private SmsManagerFactory smsManagerFactory;

    @Inject
    private LmisServer lmisServer;

    @Inject
    SharedPreferences sharedPreferences;

    public boolean send(DataValueSet dataValueSet) {
        String smsGatewayNumber = sharedPreferences.getString(SMS_GATEWAY_NUMBER, defaultDhis2SmsNumber);

        SmsManager smsManager = smsManagerFactory.build();
        List<SmsValueSet> smsValueSets = SmsValueSet.build(dataValueSet);
        i("SMS Sync", format("Start syncing through SMS, %d messages in total", smsValueSets.size()));
        for (SmsValueSet smsValueSet : smsValueSets) {
            try {
                i("SMS Sync", format("Sending to [%s] ==> %s", smsGatewayNumber, smsValueSet));
                smsManager.sendTextMessage(smsGatewayNumber, null, smsValueSet.toString(), null, null);
            } catch (Exception e) {
                e("SMS Sync", format("Failed to send smsValueSet: %s", smsValueSet), e);
                return false;
            }
        }
        return true;
    }

    public void syncGatewayNumber() {
        User user = userService.getRegisteredUser();
        String smsGatewayNumber = lmisServer.fetchPhoneNumberConstant(user, SMS_GATEWAY_NUMBER, defaultDhis2SmsNumber);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SMS_GATEWAY_NUMBER, smsGatewayNumber);
        editor.commit();
    }
}
