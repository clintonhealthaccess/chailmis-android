package org.clintonhealthaccess.lmis.app.sms;

import android.telephony.SmsManager;

public class SmsManagerFactory {
    public SmsManager build() {
        return SmsManager.getDefault();
    }
}
