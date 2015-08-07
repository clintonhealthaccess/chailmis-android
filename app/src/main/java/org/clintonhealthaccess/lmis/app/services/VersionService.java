package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;
import android.util.Log;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.api.FDroid;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;

public class VersionService {

    @Inject
    private LmisServer lmisServer;

    @Inject
    Context context;

    public Boolean shouldUpgrade() {
        try {
            String remoteVersion = lmisServer.fetchLatestVersion();
            String currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return FDroid.checkVersion(remoteVersion, currentVersion);
        } catch (Exception e) {
            Log.d("Check Latest Version", "Couldn't get remote version", e);
            return false;
        }
    }
}