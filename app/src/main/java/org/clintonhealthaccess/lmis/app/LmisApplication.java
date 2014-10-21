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

package org.clintonhealthaccess.lmis.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.inject.Inject;

import net.danlew.android.joda.JodaTimeAndroid;

import org.clintonhealthaccess.lmis.app.backgroundServices.AlertsGenerationIntentService;
import org.clintonhealthaccess.lmis.app.backgroundServices.SmsSyncIntentService;
import org.clintonhealthaccess.lmis.app.config.GuiceConfigurationModule;
import org.clintonhealthaccess.lmis.app.services.AllocationService;
import org.clintonhealthaccess.lmis.app.services.CategoryService;

import java.util.Calendar;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static roboguice.RoboGuice.DEFAULT_STAGE;
import static roboguice.RoboGuice.getInjector;
import static roboguice.RoboGuice.newDefaultRoboModule;
import static roboguice.RoboGuice.setBaseApplicationInjector;

public class LmisApplication extends Application {
    @Inject
    private CategoryService categoryService;

    @Inject
    Context context;

    @Inject
    AlarmManager alarmManager;

    @Inject
    private AllocationService allocationService;

    @Override
    public void onCreate() {
        super.onCreate();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this), new GuiceConfigurationModule());
        JodaTimeAndroid.init(this);
        getInjector(this).injectMembersWithoutViews(this);
        loadAllCommoditiesToCache();
        loadAllAllocationstoCache();
        setupAlertsService();
        setupSmsSyncingService();
    }

    private void setupSmsSyncingService() {
        setupSmsSyncingAt(1);
        setupSmsSyncingAt(13);
    }

    private void setupSmsSyncingAt(int hourOfDay) {
        Intent smsSyncingServiceIntent = new Intent(context, SmsSyncIntentService.class);
        smsSyncingServiceIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, smsSyncingServiceIntent, FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setRepeating(RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL_DAY, pendingIntent);
    }

    private void setupAlertsService() {
        Log.i("Alert", "Started");
        Intent syncDataServiceIntent = new Intent(context, AlertsGenerationIntentService.class);
        syncDataServiceIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, syncDataServiceIntent,
                FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        alarmManager.setRepeating(RTC_WAKEUP,
                calendar.getTimeInMillis(), 2 * 60 * 1000, pendingIntent);
        Log.i("Alert", "Finished");
    }

    private void loadAllCommoditiesToCache() {
        categoryService.all();
    }

    private void loadAllAllocationstoCache() {
        allocationService.all();
    }
}
