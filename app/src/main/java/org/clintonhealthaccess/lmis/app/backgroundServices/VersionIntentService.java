package org.clintonhealthaccess.lmis.app.backgroundServices;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.activities.UpgradeActivity;
import org.clintonhealthaccess.lmis.app.events.CheckVersionEvent;
import org.clintonhealthaccess.lmis.app.models.api.FDroid;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

import de.greenrobot.event.EventBus;
import roboguice.service.RoboIntentService;

public class VersionIntentService extends RoboIntentService {

    @Inject
    private LmisServer lmisServer;
    private DownloadBroadcastReceiver receiver;

    public VersionIntentService() {
        super("VersionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String currentVersion = null;
        String remoteVersion = null;
        try {
            remoteVersion = lmisServer.fetchLatestVersion();
            currentVersion = getBaseContext().getPackageManager().getPackageInfo(getBaseContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.d("Check Latest Version", "Couldn't get version", e);
            return;
        }
        if (FDroid.checkVersion(remoteVersion, currentVersion)) {
            if (receiver == null) {
                receiver = new DownloadBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
                registerReceiver(receiver, intentFilter);
            }
            receiver.setLatestVersion(remoteVersion);
            EventBus.getDefault().post(new CheckVersionEvent(remoteVersion));
            popupUpgradeDialog(remoteVersion);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    private void popupUpgradeDialog(final String latestVersion) {
        Intent intent = new Intent(getBaseContext(), UpgradeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(UpgradeActivity.LATEST_VERSION_CODE, latestVersion);
        startActivity(intent);
    }
}
