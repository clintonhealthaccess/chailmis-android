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
