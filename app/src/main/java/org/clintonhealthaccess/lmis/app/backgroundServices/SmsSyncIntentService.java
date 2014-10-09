package org.clintonhealthaccess.lmis.app.backgroundServices;

import android.content.Intent;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.services.CommoditySnapshotService;
import org.clintonhealthaccess.lmis.app.services.UserService;

import roboguice.service.RoboIntentService;

public class SmsSyncIntentService extends RoboIntentService {
    @Inject
    private CommoditySnapshotService commoditySnapshotService;

    @Inject
    private UserService userService;

    public SmsSyncIntentService(String name) {
        super(name);
    }

    public SmsSyncIntentService() {
        super("SmsSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        commoditySnapshotService.syncWithServerThroughSms(userService.getRegisteredUser());
    }
}
