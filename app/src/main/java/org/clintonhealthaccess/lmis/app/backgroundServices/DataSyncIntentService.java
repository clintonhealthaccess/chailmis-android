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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.app.services.UserService;

import roboguice.service.RoboIntentService;

import static android.util.Log.d;

public class DataSyncIntentService extends RoboIntentService {
    public static final int NOTIFICATION_ID = 0;
    @Inject
    private UserService userService;

    @Inject
    private CommodityService commodityService;

    @Inject
    private StockService stockService;

    @Inject
    private OrderService orderService;

    @Inject
    NotificationManager notificationManager;

    public DataSyncIntentService(String name) {
        super(name);
    }

    public DataSyncIntentService() {
        super("CHAI LMIS DataSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = userService.getRegisteredUser();
        sendNotificationMessage("Syncing Commodities");
        commodityService.initialise(user);
        sendNotificationMessage("Syncing Order Reasons");
        orderService.syncOrderReasons();
        sendNotificationMessage("Syncing Order Types");
        orderService.syncOrderTypes();
        sendNotificationMessage("Sync Complete");
    }

    private void sendNotificationMessage(String message) {
        d("Background Sync", message);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentTitle("LMIS Data Sync").setContentText(message).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
